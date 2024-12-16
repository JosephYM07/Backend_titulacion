package com.tesis.tigmotors.enums;

/**
 * Enum que define los estados válidos para el pago de una factura.
 */
public enum EstadoPago {
    PENDIENTE_PAGO,
    VALOR_PAGADO;

    /**
     * Valida si el estado pertenece a los estados de pago válidos.
     *
     * @param estado El estado a validar.
     * @return true si el estado pertenece a los estados de pago, false de lo contrario.
     */
    public static boolean isPagoEstado(String estado) {
        try {
            EstadoPago.valueOf(estado.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

