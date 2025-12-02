import { Button, Card, Stack } from '@mantine/core';
import { useState, useEffect } from 'react';
import api from '../../../api';
import { useNavigate } from "react-router-dom";

function AdminDashboard() {
    const [users, setUsers] = useState([]);
    const nav = useNavigate();

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const response = await api.get('/api/usuarios', {
                    withCredentials: true,
                });
                setUsers(response.data);
            } catch (error) {
                console.error('Error fetching users:', error);
            }
        };

        fetchUsers();
    }, []);

    const goToPurchases = () => {
        nav("/user/purchases");
    };

    return (

        <div>
            <Stack>
                <Card shadow="sm" withBorder radius="md">
                    <div className="flex justify-between items-center">
                        <h2>Admin Dashboard</h2>
                        <Button onClick={goToPurchases}>My Purchases</Button>
                        <Button onClick={() => nav("/admin/create-movie")} ml="md">Create Movie</Button>
                    </div>
                </Card>

                {users.length === 0 ? (
                    <p>Loading...</p>
                ) : (
                    <Stack gap="sm">
                        {users.map((user) => (
                            <Card key={user.id} shadow="lg" withBorder radius="md">
                                <h3>Hello {user.name}!</h3>
                                <p>Birthdate: {user.birth_date}</p>
                            </Card>
                        ))}
                    </Stack>
                )}
            </Stack>
        </div>
    );
}

export default AdminDashboard;
