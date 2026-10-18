package cl.huellitas.vetturno.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String mensaje) { super(mensaje); }
}
