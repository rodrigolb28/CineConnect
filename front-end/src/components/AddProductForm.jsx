import { useState, useEffect } from 'react';
import {
    Card,
    TextInput,
    NumberInput,
    Select,
    Button,
    Group,
    Stack,
    FileInput,
    MultiSelect,
    Title,
    Alert
} from '@mantine/core';
import { IconUpload, IconCheck, IconAlertCircle } from '@tabler/icons-react';
import api from '../api';

function AddProductForm() {
    const [formData, setFormData] = useState({
        name: '',
        type: 'Food',
        price: 0,
        quantity: 0,
        available: true
    });
    const [imageFile, setImageFile] = useState(null);
    const [addons, setAddons] = useState([]);
    const [selectedAddons, setSelectedAddons] = useState([]);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        // Fetch available addons
        const fetchAddons = async () => {
            try {
                const response = await api.get('/api/products/addons');
                setAddons(response.data.map(addon => ({
                    value: addon.productId,
                    label: `${addon.name} ($${addon.price.toFixed(2)})`
                })));
            } catch (error) {
                console.error('Error fetching addons:', error);
            }
        };
        fetchAddons();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validation
        if (!formData.name || !formData.price || !formData.quantity || !imageFile) {
            setError('Please fill in all required fields and select an image');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(false);

        try {
            // Create FormData for multipart request
            const formDataToSend = new FormData();

            // Create the product JSON
            const productData = {
                name: formData.name,
                type: formData.type,
                price: formData.price,
                quantity: formData.quantity,
                available: formData.available,
                productId: null,
                sessionId: null,
                imageUrl: null,
                addOns: selectedAddons.length > 0 ? selectedAddons : null,
                seatNumber: null
            };

            // Append product as JSON blob
            formDataToSend.append('product', new Blob([JSON.stringify(productData)], {
                type: 'application/json'
            }));

            // Append image file
            formDataToSend.append('file', imageFile);

            // Send request
            await api.post('/api/products/admin', formDataToSend, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
                withCredentials: true
            });

            setSuccess(true);

            // Reset form
            setFormData({
                name: '',
                type: 'Food',
                price: 0,
                quantity: 0,
                available: true
            });
            setImageFile(null);
            setSelectedAddons([]);

            // Clear success message after 3 seconds
            setTimeout(() => setSuccess(false), 3000);
        } catch (error) {
            console.error('Error creating product:', error);
            setError(error.response?.data?.message || 'Failed to create product. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Card shadow="sm" padding="lg" radius="md" withBorder>
            <Title order={3} mb="md">Add New Product</Title>

            {success && (
                <Alert icon={<IconCheck size={16} />} title="Success!" color="green" mb="md">
                    Product created successfully!
                </Alert>
            )}

            {error && (
                <Alert icon={<IconAlertCircle size={16} />} title="Error" color="red" mb="md">
                    {error}
                </Alert>
            )}

            <form onSubmit={handleSubmit}>
                <Stack gap="md">
                    <Select
                        label="Product Type"
                        placeholder="Select type"
                        required
                        data={[
                            { value: 'Food', label: 'Food' },
                            { value: 'Drink', label: 'Drink' }
                        ]}
                        value={formData.type}
                        onChange={(value) => setFormData({ ...formData, type: value })}
                    />

                    <TextInput
                        label="Product Name"
                        placeholder="e.g., Popcorn, Soda"
                        required
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    />

                    <NumberInput
                        label="Price"
                        placeholder="0.00"
                        required
                        min={0}
                        step={0.01}
                        prefix="$"
                        decimalScale={2}
                        value={formData.price}
                        onChange={(value) => setFormData({ ...formData, price: value })}
                    />

                    <NumberInput
                        label="Initial Quantity"
                        placeholder="0"
                        required
                        min={0}
                        value={formData.quantity}
                        onChange={(value) => setFormData({ ...formData, quantity: value })}
                    />

                    <FileInput
                        label="Product Image"
                        placeholder="Click to upload image"
                        required
                        accept="image/*"
                        leftSection={<IconUpload size={16} />}
                        value={imageFile}
                        onChange={setImageFile}
                    />

                    <MultiSelect
                        label="Add-ons (Optional)"
                        placeholder="Select add-ons"
                        data={addons}
                        value={selectedAddons}
                        onChange={setSelectedAddons}
                        searchable
                        clearable
                    />

                    <Group justify="flex-end" mt="md">
                        <Button
                            type="submit"
                            loading={loading}
                            disabled={loading}
                        >
                            Create Product
                        </Button>
                    </Group>
                </Stack>
            </form>
        </Card>
    );
}

export default AddProductForm;
