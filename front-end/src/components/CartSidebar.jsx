import { useState } from 'react';
import { Drawer, ScrollArea, Stack, Group, Text, Button, ActionIcon, Image, Divider, Box, Loader } from '@mantine/core';
import { IconTrash, IconMinus, IconPlus } from '@tabler/icons-react';
import { useCart } from "../context/CartContext";

function CartSidebar({ opened, onClose }) {
    const { cart, updateQuantity, removeFromCart, getTotalPrice } = useCart();
    const [isProcessing, setIsProcessing] = useState(false);

    const handleUpdateQuantity = async (itemId, newQuantity) => {
        setIsProcessing(true);
        await updateQuantity(itemId, newQuantity);
        setIsProcessing(false);
    };

    const handleRemoveFromCart = async (itemId) => {
        setIsProcessing(true);
        await removeFromCart(itemId);
        setIsProcessing(false);
    };

    return (
        <Drawer
            opened={opened}
            onClose={onClose}
            title="Shopping Cart"
            position="right"
            padding="md"
            size="md"
        >
            <ScrollArea h="calc(100vh - 150px)" type="auto">
                {cart.length === 0 ? (
                    <Text c="dimmed" ta="center" mt="xl">Your cart is empty.</Text>
                ) : (
                    <Stack spacing="md">
                        {cart.map((item) => (
                            <Box key={item.id} style={{ borderBottom: '1px solid #eee', paddingBottom: '10px' }}>
                                <Group align="flex-start" noWrap>
                                    <Image
                                        src={item.product.imageUrl || "https://placehold.co/100x100?text=No+Image"}
                                        w={60}
                                        h={60}
                                        radius="sm"
                                        fit="contain"
                                        style={{ objectPosition: 'center' }}
                                        alt={item.product.name}
                                    />
                                    <div style={{ flex: 1 }}>
                                        <Text size="sm" fw={500}>{item.product.name}</Text>
                                        <Text size="xs" c="dimmed">${item.product.price.toFixed(2)}</Text>

                                        {item.addons && item.addons.length > 0 && (
                                            <Box mt={4}>
                                                <Text size="xs" fw={500}>Addons:</Text>
                                                {item.addons.map((addon, index) => (
                                                    <Text key={index} size="xs" c="dimmed" pl={8}>
                                                        + {addon.name} (${addon.price.toFixed(2)})
                                                    </Text>
                                                ))}
                                            </Box>
                                        )}
                                    </div>
                                    <ActionIcon color="red" variant="subtle" onClick={() => handleRemoveFromCart(item.id)} disabled={isProcessing}>
                                        <IconTrash size={16} />
                                    </ActionIcon>
                                </Group>

                                <Group position="apart" mt="xs">
                                    <Group spacing={5}>
                                        <ActionIcon
                                            size="sm"
                                            variant="default"
                                            onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)}
                                            disabled={isProcessing}
                                        >
                                            <IconMinus size={12} />
                                        </ActionIcon>
                                        <Text size="sm" fw={500}>{item.quantity}</Text>
                                        <ActionIcon
                                            size="sm"
                                            variant="default"
                                            onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)}
                                            disabled={isProcessing}
                                        >
                                            <IconPlus size={12} />
                                        </ActionIcon>
                                    </Group>
                                    <Text fw={700} size="sm">
                                        ${((item.product.price * item.quantity) + (item.addons ? item.addons.reduce((sum, a) => sum + a.price, 0) : 0)).toFixed(2)}
                                    </Text>
                                </Group>
                            </Box>
                        ))}
                    </Stack>
                )}
            </ScrollArea>

            <Divider my="md" />

            <Group position="apart" mb="md">
                <Text fw={700} size="lg">Total</Text>
                <Text fw={700} size="lg" c="blue">${getTotalPrice().toFixed(2)}</Text>
            </Group>

            <Button fullWidth size="md" disabled={cart.length === 0} onClick={() => {
                onClose();
                window.location.href = "/checkout"; // Using window.location for simplicity or use useNavigate if inside Router context
            }}>
                Checkout
            </Button>
        </Drawer>
    );
}

export default CartSidebar;
