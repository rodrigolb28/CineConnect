import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api';

const BookingPage = () => {
    const { sessionId } = useParams();
    const navigate = useNavigate();

    const [occupiedSeats, setOccupiedSeats] = useState([]);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [isProcessing, setIsProcessing] = useState(false);
    // REMOVIDO: const [clientName, setClientName] = useState("");

    // Definição da Sala: 5 Fileiras (A-E) x 8 Colunas
    const rows = ['A', 'B', 'C', 'D', 'E'];
    const cols = [1, 2, 3, 4, 5, 6, 7, 8];

    useEffect(() => {
        api.get(`/api/tickets/occupied/${sessionId}`)
            .then(res => setOccupiedSeats(res.data))
            .catch(err => console.error("Erro ao carregar assentos:", err));
    }, [sessionId]);

    const toggleSeat = (seatLabel) => {
        if (occupiedSeats.includes(seatLabel)) return;

        if (selectedSeats.includes(seatLabel)) {
            setSelectedSeats(selectedSeats.filter(s => s !== seatLabel));
        } else {
            setSelectedSeats([...selectedSeats, seatLabel]);
        }
    };

    const handleAddToCart = async () => {
        if (selectedSeats.length === 0) return alert("Selecione pelo menos um assento.");

        setIsProcessing(true);

        try {
            // Para cada assento selecionado, adiciona ao carrinho
            // Isso pode ser otimizado para enviar uma lista, mas o backend atual espera um por vez ou precisamos adaptar
            // O backend aceita um item por vez no endpoint /items (modificado para TICKET)

            // Vamos fazer um loop sequencial para garantir
            for (const seat of selectedSeats) {
                await api.post('/api/cart/items', {
                    type: "TICKET",
                    sessionId: Number(sessionId),
                    seatNumber: seat,
                    price: 25.00, // Preço fixo por enquanto ou vir da sessão
                    quantity: 1
                });
            }

            alert("Ingressos adicionados ao carrinho!");
            navigate('/store'); // Redireciona para a loja/carrinho
        } catch (error) {
            console.error(error);
            alert("Erro ao adicionar ao carrinho: " + (error.response?.data || "Tente novamente."));
        } finally {
            setIsProcessing(false);
        }
    };

    // --- Definição de Cores Minimalistas ---
    const theme = {
        bg: '#ffffff',
        text: '#333333',
        textLight: '#777777',
        seatAvailable: '#e0e0e0', // Cinza claro
        seatSelected: '#2563eb',  // Azul vibrante limpo
        seatOccupied: '#d1d5db', // Cinza médio (ou use um vermelho fosco: '#ef444480')
        seatTextSelected: '#ffffff',
        buttonBg: '#2563eb',
    };

    return (
        <div style={{ padding: '40px 20px', color: theme.text, backgroundColor: theme.bg, minHeight: '100vh', fontFamily: 'sans-serif' }}>
            <div style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'center' }}>
                <h2 style={{ fontWeight: '300', fontSize: '2rem', marginBottom: '10px' }}>Seleção de Assentos</h2>
                <p style={{ color: theme.textLight, marginBottom: '40px' }}>Sessão #{sessionId}</p>

                {/* TELA DO CINEMA */}
                <div style={{
                    margin: '0 auto 40px auto',
                    width: '80%',
                    height: '8px',
                    backgroundColor: '#e5e7eb', // Cinza bem claro
                    borderRadius: '4px',
                    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' // Sombra suave para baixo
                }}></div>


                {/* GRID DE ASSENTOS */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', alignItems: 'center', marginBottom: '50px' }}>
                    {rows.map(row => (
                        <div key={row} style={{ display: 'flex', gap: '12px' }}>
                            {cols.map(col => {
                                const seatLabel = `${row}${col}`;
                                const isOccupied = occupiedSeats.includes(seatLabel);
                                const isSelected = selectedSeats.includes(seatLabel);

                                let bgColor = theme.seatAvailable;
                                let textColor = theme.text;
                                let cursorStyle = 'pointer';

                                if (isOccupied) {
                                    bgColor = theme.seatOccupied;
                                    textColor = '#9ca3af'; // Texto cinza apagado
                                    cursorStyle = 'not-allowed';
                                } else if (isSelected) {
                                    bgColor = theme.seatSelected;
                                    textColor = theme.seatTextSelected;
                                }

                                return (
                                    <button
                                        key={seatLabel}
                                        disabled={isOccupied || isProcessing}
                                        onClick={() => toggleSeat(seatLabel)}
                                        style={{
                                            width: '45px',
                                            height: '45px',
                                            backgroundColor: bgColor,
                                            border: 'none',
                                            borderRadius: '8px', // Bordas mais arredondadas
                                            color: textColor,
                                            fontWeight: isSelected ? '600' : '400',
                                            fontSize: '0.9rem',
                                            cursor: isOccupied || isProcessing ? 'not-allowed' : cursorStyle,
                                            transition: 'all 0.2s ease',
                                            boxShadow: isSelected ? '0 4px 6px -1px rgba(37, 99, 235, 0.3)' : 'none',
                                            opacity: isProcessing ? 0.7 : 1
                                        }}
                                    >
                                        {seatLabel}
                                    </button>
                                );
                            })}
                        </div>
                    ))}
                </div>

                {/* RESUMO E COMPRA (Sem o box escuro em volta) */}
                <div style={{ borderTop: '1px solid #eee', paddingTop: '30px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '15px' }}>
                    <div style={{ fontSize: '1.1rem' }}>
                        <span style={{ color: theme.textLight }}>Selecionados: </span>
                        <span style={{ fontWeight: '600' }}>
                            {selectedSeats.length > 0 ? selectedSeats.join(', ') : 'Nenhum'}
                        </span>
                    </div>

                    <div style={{ fontSize: '1.5rem', fontWeight: '300', marginBottom: '10px' }}>
                        Total: R$ {selectedSeats.length * 25},00
                    </div>

                    {/* INPUT DE NOME REMOVIDO AQUI */}

                    <button
                        onClick={handleAddToCart}
                        disabled={selectedSeats.length === 0 || isProcessing}
                        style={{
                            padding: '15px 40px',
                            backgroundColor: (selectedSeats.length === 0 || isProcessing) ? '#ccc' : theme.buttonBg,
                            color: 'white',
                            border: 'none',
                            borderRadius: '50px', // Botão estilo pílula
                            fontWeight: '600',
                            fontSize: '1rem',
                            cursor: (selectedSeats.length === 0 || isProcessing) ? 'not-allowed' : 'pointer',
                            transition: 'background-color 0.2s',
                            boxShadow: (selectedSeats.length === 0 || isProcessing) ? 'none' : '0 4px 6px -1px rgba(37, 99, 235, 0.4)'
                        }}
                    >
                        {isProcessing ? 'Processando...' : 'Adicionar ao Carrinho'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default BookingPage;
