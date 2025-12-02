import { useState } from 'react';
import { TextInput, NumberInput, Textarea, Button, FileInput, Group, Title, Container, Paper } from '@mantine/core';
import { useForm } from '@mantine/form';
import api from '../../../api';
import { useNavigate } from 'react-router-dom';

function CreateMovie() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);

    const form = useForm({
        initialValues: {
            title: '',
            synopsis: '',
            genre: '',
            duration: 0,
            rating: 0,
            file: null,
        },
        validate: {
            title: (value) => (value.length < 2 ? 'Title must have at least 2 letters' : null),
            duration: (value) => (value <= 0 ? 'Duration must be greater than 0' : null),
            file: (value) => (value === null ? 'Cover image is required' : null),
        },
    });

    const handleSubmit = async (values) => {
        setLoading(true);
        const formData = new FormData();

        // Create the movie object as a JSON blob
        const movieData = {
            title: values.title,
            synopsis: values.synopsis,
            genre: values.genre,
            duration: values.duration,
            rating: values.rating,
        };

        const movieBlob = new Blob([JSON.stringify(movieData)], {
            type: 'application/json'
        });

        formData.append('movie', movieBlob);
        formData.append('file', values.file);

        try {
            await api.post('/api/movie', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            alert('Movie created successfully!');
            navigate('/admin/dashboard');
        } catch (error) {
            console.error(error);
            alert('Failed to create movie. Check console for details.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container size={420} my={40}>
            <Title align="center">Register New Movie</Title>
            <Paper withBorder shadow="md" p={30} mt={30} radius="md">
                <form onSubmit={form.onSubmit(handleSubmit)}>
                    <TextInput
                        label="Title"
                        placeholder="Movie Title"
                        required
                        {...form.getInputProps('title')}
                    />
                    <Textarea
                        label="Synopsis"
                        placeholder="Movie Synopsis"
                        mt="md"
                        {...form.getInputProps('synopsis')}
                    />
                    <TextInput
                        label="Genre"
                        placeholder="Action, Drama, etc."
                        mt="md"
                        {...form.getInputProps('genre')}
                    />
                    <NumberInput
                        label="Duration (minutes)"
                        mt="md"
                        min={1}
                        {...form.getInputProps('duration')}
                    />
                    <NumberInput
                        label="Rating (0-10)"
                        mt="md"
                        min={0}
                        max={10}
                        precision={1}
                        step={0.1}
                        {...form.getInputProps('rating')}
                    />
                    <FileInput
                        label="Cover Image"
                        placeholder="Upload image"
                        mt="md"
                        accept="image/png,image/jpeg"
                        {...form.getInputProps('file')}
                    />

                    <Group position="center" mt="xl">
                        <Button type="submit" loading={loading}>
                            Register Movie
                        </Button>
                    </Group>
                </form>
            </Paper>
        </Container>
    );
}

export default CreateMovie;
