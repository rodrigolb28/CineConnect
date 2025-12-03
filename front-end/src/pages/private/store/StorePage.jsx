import { useEffect, useState } from 'react';
import { Card, Image, Text, Badge, Button, Group, SimpleGrid, Container, Title, Loader, Center, Modal, Checkbox, Stack, ActionIcon } from '@mantine/core';
import { IconShoppingCart, IconShoppingBag } from '@tabler/icons-react';
import { useDisclosure } from '@mantine/hooks';
import api from '../../../api';
import { useCart } from '../../../context/CartContext';
import CartSidebar from '../../../components/CartSidebar';

function StorePage() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isProcessing, setIsProcessing] = useState(false);
    const { addToCart, cart } = useCart();

    // Addon Modal State
    const [opened, { open, close }] = useDisclosure(false);
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [selectedAddons, setSelectedAddons] = useState([]);

    // Cart Sidebar State
    const [cartOpened, { open: openCart, close: closeCart }] = useDisclosure(false);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [productsRes] = await Promise.all([
                    api.get('/api/products')
                ]);
                setProducts(productsRes.data);
            } catch (error) {
                console.error("Error fetching data:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const handleAddToCartClick = async (product) => {
        if (product.type === 'Food') {
            setSelectedProduct(product);
            setSelectedAddons([]);
            open();
        } else {
            setIsProcessing(true);
            await addToCart(product);
            setIsProcessing(false);
        }
    };

    const confirmAddToCart = async () => {
        if (selectedProduct) {
            setIsProcessing(true);
            await addToCart(selectedProduct, 1, selectedAddons);
            setIsProcessing(false);
            close();
            setSelectedProduct(null);
            setSelectedAddons([]);
        }
    };

    const toggleAddon = (addonId) => {
        setSelectedAddons((prev) =>
            prev.includes(addonId)
                ? prev.filter(id => id !== addonId)
                : [...prev, addonId]
        );
    };

    if (loading) {
        return (
            <Center h={300}>
                <Loader size="xl" />
            </Center>
        );
    }

    return (
        <Container size="xl" py="xl">
            <Title order={1} mb="xl" ta="center">CineConnect Store</Title>
            <SimpleGrid cols={{ base: 1, sm: 2, md: 3, lg: 4 }} spacing="lg">
                {products.map((product) => (
                    <Card key={product.productId} shadow="sm" padding="lg" radius="md" withBorder>
                        <Card.Section>
                            <Image
                                src={product.imageUrl ? `http://localhost:8080${product.imageUrl}` : "https://placehold.co/300x200?text=No+Image"}
                                h={200}
                                w="100%"
                                fit="cover"
                                alt={product.name}
                                fallbackSrc="https://placehold.co/300x200?text=No+Image"
                                style={{ backgroundColor: '#fff' }}
                            />
                        </Card.Section>

                        <Group justify="space-between" mt="md" mb="xs">
                            <Text fw={500}>{product.name}</Text>
                            <Badge color="pink" variant="light">
                                {product.type}
                            </Badge>
                        </Group>

                        <Group justify="space-between" mb="md">
                            <Text size="sm" c="dimmed">
                                ${product.price.toFixed(2)}
                            </Text>
                            <Badge color={product.available ? "green" : "red"} variant="light">
                                {product.available ? `In Stock (${product.quantity})` : "Out of Stock"}
                            </Badge>
                        </Group>

                        <Button
                            color="blue"
                            fullWidth
                            mt="md"
                            radius="md"
                            leftSection={!isProcessing && <IconShoppingCart size={18} />}
                            onClick={() => handleAddToCartClick(product)}
                            disabled={!product.available || isProcessing}
                            loading={isProcessing && product.type !== 'Food'} // Show loader only for direct add (non-food or if logic changes)
                        >
                            {product.available ? (isProcessing && product.type !== 'Food' ? 'Adding...' : 'Add to Cart') : 'Unavailable'}
                        </Button>
                    </Card>
                ))}
            </SimpleGrid>

            <Modal opened={opened} onClose={close} title={`Addons for ${selectedProduct?.name}`}>
                <Stack>
                    <Text size="sm" mb="xs">Select extra toppings or add-ons:</Text>
                    {selectedProduct?.addOns?.map(addon => (
                        <Checkbox
                            key={addon.productId}
                            label={`${addon.name} (+$${addon.price.toFixed(2)})`}
                            checked={selectedAddons.includes(addon.productId)}
                            onChange={() => toggleAddon(addon.productId)}
                        />
                    ))}
                    <Button fullWidth mt="md" onClick={confirmAddToCart} loading={isProcessing} disabled={isProcessing}>
                        Confirm & Add to Cart
                    </Button>
                </Stack>
            </Modal>

            <CartSidebar opened={cartOpened} onClose={closeCart} />

            <ActionIcon
                color="blue"
                size="xl"
                radius="xl"
                variant="filled"
                style={{ position: 'fixed', bottom: 30, right: 30, zIndex: 1000 }}
                onClick={openCart}
            >
                <IconShoppingBag size={24} />
                {cart.length > 0 && (
                    <Badge
                        size="xs"
                        circle
                        color="red"
                        style={{ position: 'absolute', top: -5, right: -5 }}
                    >
                        {cart.length}
                    </Badge>
                )}
            </ActionIcon>
        </Container>
    );
}

export default StorePage;
