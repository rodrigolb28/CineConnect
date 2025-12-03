import React, { useState, useEffect, useRef } from "react";
import { loadStripe } from "@stripe/stripe-js";
import { Elements } from "@stripe/react-stripe-js";
import api from "../../../api";
import CheckoutForm from "../../../components/CheckoutForm";
import { useCart } from "../../../context/CartContext";

// Make sure to call loadStripe outside of a component’s render to avoid
// recreating the Stripe object on every render.
// This is your test publishable API key.
const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

export function PaymentPage() {
    const [clientSecret, setClientSecret] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const { cart } = useCart();
    const paymentIntentCreated = useRef(false);

    useEffect(() => {
        // Create PaymentIntent as soon as the page loads (only once)
        const createPaymentIntent = async () => {
            if (cart.length === 0) {
                setIsLoading(false);
                return;
            }

            // Prevent duplicate payment intent creation
            if (paymentIntentCreated.current) {
                return;
            }

            try {
                setIsLoading(true);
                paymentIntentCreated.current = true;
                // Map cart to ProductRecord format expected by backend
                const productRecords = cart.map(item => ({
                    productId: item.product.id,
                    name: item.product.name,
                    type: item.product.type,
                    price: item.product.price,
                    quantity: item.quantity,
                    sessionId: item.product.sessionId,
                    imageUrl: item.product.imageUrl,
                    addOns: item.addons ? item.addons.map(addon => ({
                        productId: addon.id,
                        name: addon.name,
                        type: addon.type,
                        price: addon.price,
                        imageUrl: addon.imageUrl
                    })) : []
                }));

                const response = await api.post("/api/stripe/create-payment-intent", productRecords);
                setClientSecret(response.data);
                setIsLoading(false);
            } catch (err) {
                console.error("Error creating payment intent:", err);
                setError("Failed to initialize payment. Please try again.");
                setIsLoading(false);
                paymentIntentCreated.current = false; // Reset on error to allow retry
            }
        };

        createPaymentIntent();
    }, [cart]);

    const appearance = {
        theme: 'stripe',
    };
    const options = {
        clientSecret,
        appearance,
    };

    if (isLoading) {
        return <div className="p-8 text-center text-lg">Loading Payment...</div>;
    }

    if (error) {
        return <div className="p-8 text-center text-lg text-red-600">Error: {error}</div>;
    }

    if (!clientSecret) {
        return <div className="p-8 text-center text-lg">Cart is empty or payment initialization failed.</div>;
    }

    return (
        <div className="p-8 flex flex-col items-center">
            <h1 className="text-2xl font-bold mb-6">Payment</h1>

            <div className="w-full max-w-4xl mb-8">
                <h2 className="text-xl font-semibold mb-4">Your Cart</h2>
                <div className="bg-white shadow rounded-lg p-6">
                    {cart.map((item, index) => (
                        <div key={index} className="flex justify-between items-center border-b py-4 last:border-b-0">
                            <div className="flex items-center">
                                {item.product.imageUrl && (
                                    <img src={`http://localhost:8080${item.product.imageUrl}`} alt={item.product.name} className="w-16 h-16 object-cover rounded mr-4" />
                                )}
                                <div>
                                    <h3 className="font-medium">{item.product.name}</h3>
                                    <p className="text-gray-500 text-sm">{item.product.type}</p>
                                    {item.quantity > 1 && (
                                        <p className="text-gray-600 text-sm">Quantity: {item.quantity}</p>
                                    )}
                                    {item.addons && item.addons.length > 0 && (
                                        <div className="text-sm text-gray-500 mt-1">
                                            Add-ons: {item.addons.map(addon => addon.name).join(", ")}
                                        </div>
                                    )}
                                </div>
                            </div>
                            <div className="text-right">
                                <p className="font-medium">${(item.product.price * item.quantity).toFixed(2)}</p>
                                {item.addons && item.addons.length > 0 && (
                                    <p className="text-sm text-gray-500">
                                        + ${item.addons.reduce((sum, addon) => sum + addon.price, 0).toFixed(2)}
                                    </p>
                                )}
                            </div>
                        </div>
                    ))}
                    <div className="flex justify-between items-center pt-4 mt-4 border-t font-bold text-lg">
                        <span>Total</span>
                        <span>
                            ${cart.reduce((total, item) => {
                                const itemTotal = (item.product.price * item.quantity) + (item.addons ? item.addons.reduce((sum, addon) => sum + addon.price, 0) : 0);
                                return total + itemTotal;
                            }, 0).toFixed(2)}
                        </span>
                    </div>
                </div>
            </div>

            {clientSecret && (
                <Elements options={options} stripe={stripePromise}>
                    <CheckoutForm />
                </Elements>
            )}
        </div>
    );
}

export default PaymentPage;