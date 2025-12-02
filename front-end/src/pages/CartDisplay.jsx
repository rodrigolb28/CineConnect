import { useEffect, useState } from 'react';
import {
    Card,
    Image,
    Text,
    Badge,
    Button,
    Group,
    SimpleGrid,
    Container,
    Title,
    Loader,
    Center,
    Table
} from '@mantine/core';
import { IconShoppingCart } from '@tabler/icons-react';
import { CartProvider, useCart } from "../context/CartContext";
import { Tab } from "@material-tailwind/react";

function CartDisplay() {
    const { cart } = useCart();

    return (
        <Table>
            <Table.Thead>
                <Table.Tr>
                    <Table.Th>Element position</Table.Th>
                    <Table.Th>Element name</Table.Th>
                    <Table.Th>Symbol</Table.Th>
                    <Table.Th>Atomic mass</Table.Th>
                </Table.Tr>
            </Table.Thead>

            <Table.Tbody>
                {cart.map(product => (
                    <Table.Tr key={product.productId}>
                        <Table.Td>1</Table.Td>
                        <Table.Td>{product.name}</Table.Td>
                        <Table.Td>something</Table.Td>
                        <Table.Td>etc</Table.Td>
                    </Table.Tr>
                ))}
            </Table.Tbody>
        </Table>
    )
}



export default CartDisplay;
