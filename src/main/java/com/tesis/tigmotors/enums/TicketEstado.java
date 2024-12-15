package com.tesis.tigmotors.enums;

/**
 * Enum que representa los posibles estados de un ticket y las prioridades asociadas.
 */
public enum TicketEstado {
    TRABAJO_PENDIENTE,
    TRABAJO_EN_PROGRESO,
    TRABAJO_TERMINADO,
    RECHAZADO,
    VALOR_PAGADO,
    PENDIENTE_PAGO,
    ALTA,
    MEDIA,
    BAJA;

    /**
     * Valida si el estado pertenece a los estados de trabajo.
     *
     * @param estado El estado a validar.
     * @return true si el estado pertenece a los estados de trabajo, false de lo contrario.
     */
    public static boolean isTrabajoEstado(String estado) {
        try {
            TicketEstado ticketEstado = TicketEstado.valueOf(estado.toUpperCase());
            return ticketEstado == TRABAJO_PENDIENTE ||
                    ticketEstado == TRABAJO_EN_PROGRESO ||
                    ticketEstado == TRABAJO_TERMINADO;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Valida si el estado pertenece a los estados de pago.
     *
     * @param estado El estado a validar.
     * @return true si el estado pertenece a los estados de pago, false de lo contrario.
     */
    public static boolean isPagoEstado(String estado) {
        try {
            TicketEstado ticketEstado = TicketEstado.valueOf(estado.toUpperCase());
            return ticketEstado == PENDIENTE_PAGO || ticketEstado == VALOR_PAGADO;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Valida si el estado pertenece a las prioridades de solicitud.
     *
     * @param estado La prioridad a validar.
     * @return true si el estado pertenece a las prioridades de solicitud, false de lo contrario.
     */
    public static boolean isPrioridadEstado(String estado) {
        try {
            TicketEstado ticketEstado = TicketEstado.valueOf(estado.toUpperCase());
            return ticketEstado == ALTA || ticketEstado == MEDIA || ticketEstado == BAJA;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Retorna los estados válidos para trabajos.
     *
     * @return Lista de estados válidos para trabajos.
     */
    public static String getEstadosValidosTrabajo() {
        return "TRABAJO_PENDIENTE, TRABAJO_EN_PROGRESO, TRABAJO_TERMINADO";
    }

    /**
     * Retorna los estados válidos para pagos.
     *
     * @return Lista de estados válidos para pagos.
     */
    public static String getEstadosValidosPago() {
        return "PENDIENTE_PAGO, VALOR_PAGADO";
    }

    /**
     * Retorna las prioridades válidas de solicitud.
     *
     * @return Lista de prioridades válidas.
     */
    public static String getEstadosValidosPrioridad() {
        return "ALTA, MEDIA, BAJA";
    }
}
