(() => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    const message = document.querySelector('.checkout-message');

    document.querySelectorAll('.pay-online-button').forEach(button => {
        button.addEventListener('click', async () => {
            button.disabled = true;
            if (message) message.textContent = 'Preparing secure checkout…';
            try {
                const response = await fetch(`/member/payments/${button.dataset.paymentId}/order`, {
                    method: 'POST', headers: { [csrfHeader]: csrfToken, 'Accept': 'application/json' }
                });
                const order = await response.json();
                if (!response.ok) throw new Error(order.error || 'Could not start checkout.');
                const checkout = new Razorpay({
                    key: order.keyId, amount: order.amount, currency: order.currency,
                    name: order.name, description: order.description, order_id: order.orderId,
                    prefill: order.prefill,
                    handler: async result => {
                        try {
                            if (message) message.textContent = 'Verifying payment…';
                            const verified = await fetch(`/member/payments/${button.dataset.paymentId}/verify`, {
                                method: 'POST',
                                headers: { [csrfHeader]: csrfToken, 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
                                body: new URLSearchParams(result)
                            });
                            const confirmation = await verified.json();
                            if (!verified.ok) throw new Error(confirmation.error || 'Payment could not be confirmed.');
                            if (message) message.textContent = confirmation.message;
                            window.location.reload();
                        } catch (error) {
                            button.disabled = false;
                            if (message) message.textContent = error.message || 'Payment could not be confirmed. If you were charged, contact the gym.';
                        }
                    },
                    modal: { ondismiss: () => {
                        button.disabled = false;
                        if (message) message.textContent = '';
                    } }
                });
                checkout.on('payment.failed', event => {
                    button.disabled = false;
                    if (message) message.textContent = event.error?.description || 'Payment did not complete. You can try again.';
                });
                checkout.open();
                if (message) message.textContent = '';
            } catch (error) {
                button.disabled = false;
                if (message) message.textContent = error.message || 'Could not start checkout.';
            }
        });
    });
})();
