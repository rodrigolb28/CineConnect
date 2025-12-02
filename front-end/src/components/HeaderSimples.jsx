import { ActionIcon, Container, Group, Button } from '@mantine/core';
import { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import api from "../api";

export function HeaderSimples() {
  const nav = useNavigate();
  const [user, setUser] = useState(null);

  useEffect(() => {
    const checkLogin = async () => {
      try {
        const response = await api.get("/api/me");
        setUser(response.data);
      } catch (error) {
        // Not logged in
        setUser(null);
      }
    };
    checkLogin();
  }, []);

  const gotoLogin = () => {
    nav("/login");
  }

  const gotoStore = () => {
    nav("/store");
  }

  const gotoStart = () => {
    nav("/");
  }

  const gotoRegister = () => {
    nav("/register");
  }

  const gotoDashboard = () => {
    nav("/user/dashboard");
  }

  return (
    <div className="border-b border-gray-200 py-1 bg-gray-50">
      <Container className="flex items-center justify-between">

        {/*
          AJUSTE: Agrupando a logo e o texto em um link (prática padrão)
          e usando 'gap="xs"' para manter os elementos próximos.
        */}
        <a href="/" className="flex items-center space-x-2 text-gray-900 no-underline">
          <img
            src="/logo.svg"
            style={{ height: '35px', width: "auto" }} /* Ajuste a altura para 35px */
            alt="CineConnect Logo"
          />
          <span className="text-xl font-semibold">CineConnect</span>
        </a>

        {/* Links de Navegação */}
        <Group gap={0} justify="flex-center" wrap="nowrap" className="hidden sm:flex">
          <Button variant="subtle" onClick={gotoStart} className="text-gray-500 transition hover:text-gray-500/75">
            Movies
          </Button>
          <Button variant="subtle" onClick={gotoStore} className="text-gray-500 transition hover:text-gray-500/75">
            Store
          </Button>
        </Group>

        {/* Botões de Ação */}
        <Group gap={0} justify="flex-end" wrap="nowrap">
          {user ? (
            <Button variant="filled" size="xs" onClick={gotoDashboard}>Account</Button>
          ) : (
            <>
              <Button variant="outline" size="xs" onClick={gotoRegister}>Register</Button>
              <div className="pl-1"></div>
              <Button variant="filled" size="xs" onClick={gotoLogin}>Login</Button>
            </>
          )}
        </Group>

      </Container>
    </div>
  );
}
export default HeaderSimples;