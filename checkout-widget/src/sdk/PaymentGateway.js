class PaymentGateway {
    constructor(options) {
        this.key = options.key;
        this.orderId = options.orderId;
        this.onSuccess = options.onSuccess || (() => {});
        this.onFailure = options.onFailure || (() => {});
        this.checkoutUrl = "http://localhost:3001"; // URL of your checkout service
    }

    open() {
        // 1. Create Modal Overlay
        this.modal = document.createElement('div');
        Object.assign(this.modal.style, {
            position: 'fixed', top: '0', left: '0', width: '100%', height: '100%',
            backgroundColor: 'rgba(0,0,0,0.5)', zIndex: '9999',
            display: 'flex', justifyContent: 'center', alignItems: 'center'
        });

        // 2. Create Iframe
        const iframe = document.createElement('iframe');
        iframe.src = `${this.checkoutUrl}/checkout.html?order_id=${this.orderId}&key=${this.key}&embedded=true`;
        Object.assign(iframe.style, {
            width: '400px', height: '600px', border: 'none',
            borderRadius: '8px', backgroundColor: 'white'
        });
        
        this.modal.appendChild(iframe);
        document.body.appendChild(this.modal);

        // 3. Listen for Messages
        window.addEventListener('message', this.handleMessage.bind(this));
    }

    handleMessage(event) {
        // In production, validate event.origin here!
        const { type, data } = event.data;
        if (type === 'payment_success') {
            this.onSuccess(data);
            this.close();
        } else if (type === 'payment_failed') {
            this.onFailure(data);
        } else if (type === 'close_modal') {
            this.close();
        }
    }

    close() {
        if (this.modal) {
            document.body.removeChild(this.modal);
            this.modal = null;
        }
    }
}

// Expose globally
window.PaymentGateway = PaymentGateway;
export default PaymentGateway;
