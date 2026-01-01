(function (global) {

  const HONEYPOT_FIELD_FILLED   = 'HONEYPOT_FIELD_FILLED';
  const FORM_SUBMITTED_TOO_FAST = 'FORM_SUBMITTED_TOO_FAST';
  const NO_HUMAN_INTERACTION    = 'NO_HUMAN_INTERACTION';

  class HoneypotDetector {
    constructor(config) {
      this.config = Object.assign({
        honeypotSelector: '[data-honeypot]',
        reportUrl: 'http://localhost:8080/api/honeypot/report',
        minHumanTimeMs: 1500
      }, config);

      this.startTime = Date.now();
      this.nonce = crypto.randomUUID();

      this.interactions = {
        mouse: false,
        keyboard: false,
        focus: false
      };

      this._bindEvents();
    }

    _bindEvents() {
      document.addEventListener('mousemove', () => this.interactions.mouse = true, { once: true });
      document.addEventListener('keydown', () => this.interactions.keyboard = true, { once: true });
      document.addEventListener('focusin', () => this.interactions.focus = true, { once: true });
    }

    analyzeForm(form) {
      const reasons = [];

      const honeypots = form.querySelectorAll(this.config.honeypotSelector);
      honeypots.forEach(input => {
        if (input.value && input.value.trim() !== '') {
          reasons.push(HONEYPOT_FIELD_FILLED);
        }
      });

      const elapsed = Date.now() - this.startTime;
      if (elapsed < this.config.minHumanTimeMs) {
        reasons.push(FORM_SUBMITTED_TOO_FAST);
      }

      if (!this.interactions.mouse && !this.interactions.keyboard) {
        reasons.push(NO_HUMAN_INTERACTION);
      }

      return {
        suspicious: reasons.length > 0,
        reasons,
        elapsed,
        interactions: this.interactions
      };
    }

    _canonicalString(payload) {
      return [
        payload.timestamp,
        payload.nonce,
        payload.userAgent,
        JSON.stringify(payload.result)
      ].join('|');
    }

    async _computeHash(payload) {
      const canonical = this._canonicalString(payload);
      const buffer = await crypto.subtle.digest(
        'SHA-256',
        new TextEncoder().encode(canonical)
      );
      return Array.from(new Uint8Array(buffer))
        .map(b => b.toString(16).padStart(2, '0'))
        .join('');
    }

    async report(result) {

      if (!result.suspicious) return;

      const payload = {
        timestamp: Date.now(),
        nonce: this.nonce,
        userAgent: navigator.userAgent,
        result
      };

      payload.integrityHash = await this._computeHash(payload);

      fetch(this.config.reportUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    }
  }
  global.vhoang = window.vhoang || {};
  global.vhoang.HoneypotDetector = HoneypotDetector;

})(window);
