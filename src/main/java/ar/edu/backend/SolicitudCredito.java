package ar.edu.backend;

public class SolicitudCredito {
    // puede llegar a saltar un error xq aca es idCliente enfasis en esa ultima e
    // y el codigo dado usa idClient sin e, asi q cuidadito
    private final String idCliente;
    private final String tipoCliente;
    private final double scoreFinanciero;
    private final String estado;
    // defino nueva columna
    private final String nivelMorosidad;

    private final static double TASA_BASE = 40;

    // el constructor viejo, con la declaracion de morosidad como normal
    public SolicitudCredito(String idClient, String tipoCliente, double scoreFinanciero, String estado) {
        // aca uso idClient sin la e xq ya venia definido asi
        if (idClient == null || idClient.isBlank() || tipoCliente == null || tipoCliente.isBlank()) {
            throw new IllegalArgumentException("El Id del Cliente como su Categorizacion (tipo) son requeridos");
        }
        this.idCliente = idClient;
        this.tipoCliente = tipoCliente;
        this.scoreFinanciero = scoreFinanciero;
        this.estado = estado;
        this.nivelMorosidad = "NORMAL";
    }

    // el constructor nuevo con verificacion
    public SolicitudCredito(String idCliente, String tipoCliente, double scoreFinanciero, String estado, String nievlMorosidad) {
        // aca uso idCliente con la e xq es como lo defini al principio
        if (idCliente == null || idCliente.isBlank() || tipoCliente == null || tipoCliente.isBlank()) {
            throw new IllegalArgumentException("El Id del Cliente como su Categorizacion (tipo) son requeridos");
        }

        this.idCliente = idCliente;
        this.tipoCliente = tipoCliente;
        this.scoreFinanciero = scoreFinanciero;
        this.estado = estado;
        this.nivelMorosidad = nievlMorosidad;
    }

    public static SolicitudCredito desdeCampos(String[] tokens) {
        // me fijo si no es 5 o 6 error
        if (tokens.length != 5 && tokens.length != 6)
            throw new IllegalArgumentException("Se esperan 5  o 6 columnas a procesar");

        double ingresos = Double.parseDouble(tokens[2]);
        double deudas = Double.parseDouble(tokens[3]);
        double scoreFinanciero = (1 - (deudas / ingresos)) * 100;
        // declaro morosidad como normal y si son 6 columnas le doy el valor q venga
        String morosidad = "NORMAL";
        if (tokens.length == 6) {
            morosidad = tokens[5];
        }

        // si la morosidad es normal se verifica lo q ya estaba y se devuelve solicitud de credito de constructor con 4 parametros
        if (morosidad.equals("NORMAL")) {
            if (ingresos < 1000 || ingresos > 15000)
                throw new IllegalArgumentException("Los ingresos mensuales no son aptos para solicitar el credito");
            if (deudas < 0 || deudas >= ingresos)
                throw new IllegalArgumentException("La deudas registradas no permiten solicitar el credito");

            return new SolicitudCredito(tokens[0], tokens[1], scoreFinanciero, tokens[4]);

            // si la morosidad es en mora, se aplica la nueva logica de ingresos y deudas
        } else if (morosidad.equals("EN_MORA")) {
            if (ingresos <= 25000) {
                throw new IllegalArgumentException("ingresos fuera de rango");
            }
            if ( deudas < 0 || deudas > 0.12 * ingresos) {
                throw new IllegalArgumentException("deudas fuera de rango");
            }

            return new SolicitudCredito(tokens[0], tokens[1], scoreFinanciero, tokens[4], tokens[5]);

            // si no es en mora o normal, entonces rari y tira error
        } else {
            throw new IllegalArgumentException("La morosidad es invalida");
        }
    }
    public String getIdCliente() {
        return idCliente;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public double getScoreFinanciero() {
        return scoreFinanciero;
    }

    public String getEstado() {
        return estado;
    }

    // acordarse de hacer este getter
    public String getNivelMorosidad() {
        return nivelMorosidad;
    }

    public double getTasaInteresAnual() {
        return SolicitudCredito.TASA_BASE + (100 - this.scoreFinanciero) * 0.8;
    }

}
