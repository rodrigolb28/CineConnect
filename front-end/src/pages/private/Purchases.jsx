import React, { useState, useEffect } from 'react';
import { Card, Stack, Text, Badge, Group, Image } from "@mantine/core";
import api from "../../api";

function UserPurchases() {
    const [user, setUser] = useState(null);
    const [purchases, setPurchases] = useState(null)

    useEffect(() => {
        const fetchPurchases = async () => {
            try {
                const response = await api.get("/api/me/purchases", {
                    withCredentials: true, // ensures cookies are sent
                });
                setPurchases(response.data);
            } catch (error) {
                console.error("Error fetching purchases:", error);
            }
        };

        fetchPurchases();
    }, []);

    return (
        <Stack spacing="md" p="md">
            <h1 style={{ fontSize: '1.75rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>My Purchases</h1>
            {purchases ? (
                purchases.length === 0 ? (
                    <Text c="dimmed">You haven't made any purchases yet.</Text>
                ) : (
                    purchases.map((purchase) => (
                        <Card key={purchase.id} shadow="sm" padding="sm" radius="md" withBorder>
                            <Group position="apart" mb="xs">
                                <Group spacing="xs">
                                    <Text size="sm" fw={600}>#{purchase.id.substring(0, 8)}</Text>
                                    <Badge size="sm" color={purchase.status === 'PAID' || purchase.status === 'succeeded' ? 'green' : 'yellow'}>
                                        {purchase.status}
                                    </Badge>
                                </Group>
                                <Text c="dimmed" size="xs">
                                    {new Date(purchase.createdAt).toLocaleDateString()} {new Date(purchase.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </Text>
                            </Group>

                            <table style={{ width: '100%', fontSize: '0.875rem', borderCollapse: 'collapse' }}>
                                <thead>
                                    <tr style={{ borderBottom: '1px solid #e9ecef' }}>
                                        <th style={{ textAlign: 'left', padding: '4px 8px', fontWeight: 500, color: '#868e96' }}></th>
                                        <th style={{ textAlign: 'left', padding: '4px 8px', fontWeight: 500, color: '#868e96' }}>Item</th>
                                        <th style={{ textAlign: 'center', padding: '4px 8px', fontWeight: 500, color: '#868e96' }}>Qty</th>
                                        <th style={{ textAlign: 'right', padding: '4px 8px', fontWeight: 500, color: '#868e96' }}>Price</th>
                                        <th style={{ textAlign: 'right', padding: '4px 8px', fontWeight: 500, color: '#868e96' }}>Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {purchase.items.map((item, index) => (
                                        <React.Fragment key={index}>
                                            <tr style={{ borderBottom: '1px solid #f1f3f5' }}>
                                                <td style={{ padding: '6px 8px' }}>
                                                    <img
                                                        src={item.imageUrl || "https://placehold.co/24x24?text=X"}
                                                        alt={item.name}
                                                        style={{ width: '24px', height: '24px', borderRadius: '4px', objectFit: 'cover' }}
                                                    />
                                                </td>
                                                <td style={{ padding: '6px 8px', fontWeight: 500 }}>{item.name}</td>
                                                <td style={{ padding: '6px 8px', textAlign: 'center' }}>{item.quantity}</td>
                                                <td style={{ padding: '6px 8px', textAlign: 'right' }}>${item.priceAtPurchase.toFixed(2)}</td>
                                                <td style={{ padding: '6px 8px', textAlign: 'right', fontWeight: 500 }}>
                                                    ${(item.priceAtPurchase * item.quantity).toFixed(2)}
                                                </td>
                                            </tr>
                                            {item.addons && item.addons.map((addon, addonIdx) => (
                                                <tr key={`addon-${index}-${addonIdx}`} style={{ backgroundColor: '#f8f9fa' }}>
                                                    <td style={{ padding: '4px 8px' }}></td>
                                                    <td colSpan="2" style={{ padding: '4px 8px 4px 24px', fontSize: '0.8rem', color: '#868e96' }}>
                                                        + {addon.name}
                                                    </td>
                                                    <td style={{ padding: '4px 8px', textAlign: 'right', fontSize: '0.8rem', color: '#868e96' }}>
                                                        ${addon.priceAtPurchase.toFixed(2)}
                                                    </td>
                                                    <td style={{ padding: '4px 8px', textAlign: 'right', fontSize: '0.8rem', color: '#868e96' }}>
                                                        ${addon.priceAtPurchase.toFixed(2)}
                                                    </td>
                                                </tr>
                                            ))}
                                        </React.Fragment>
                                    ))}
                                </tbody>
                                <tfoot>
                                    <tr style={{ borderTop: '2px solid #dee2e6' }}>
                                        <td colSpan="4" style={{ padding: '8px', textAlign: 'right', fontWeight: 600 }}>Total:</td>
                                        <td style={{ padding: '8px', textAlign: 'right', fontWeight: 700, fontSize: '1rem' }}>
                                            ${purchase.totalAmount.toFixed(2)}
                                        </td>
                                    </tr>
                                </tfoot>
                            </table>
                        </Card>
                    ))
                )
            ) : (
                <Text>Loading purchases...</Text>
            )}
        </Stack>
    );
}
export default UserPurchases;
