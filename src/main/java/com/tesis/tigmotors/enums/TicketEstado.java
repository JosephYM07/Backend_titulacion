package com.tesis.tigmotors.enums;

public enum TicketEstado {
    TRABAJO_PENDIENTE,
    TRABAJO_EN_PROGRESO,
    TRABAJO_TERMINADO,
    RECHAZADO,
    VALOR_PAGADO,
    PENDIENTE_PAGO;

    public static boolean isValidEstado(String estado) {
        for (TicketEstado ticketEstado : TicketEstado.values()) {
            if (ticketEstado.name().equalsIgnoreCase(estado)) {
                return true;
            }
        }
        return false;
    }
}