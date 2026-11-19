package ar.edu.backend;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CarteraCreditos {
    private final List<SolicitudCredito> creditos;

    public CarteraCreditos(List<SolicitudCredito> creditos) {
        this.creditos = List.copyOf(creditos);
    }

    public List<SolicitudCredito> filtrar(Predicate<SolicitudCredito> criterio) {
        return this.creditos.stream().filter(criterio).toList();
    }

    // cuento por el nivel de morosidad, si esta vacio devuelve un mapa vacio
    public Map<String, Long> conteoPorNivelMorosidad() {
        // agarro los creditos y le digo q me haga una coleccion
        // de estos los agrupo por la solicitud de credito, por su nivel de morosidad
        // luego le diga q me cuente cada uno. ej:
        // EN MORA 10  -   NORMAL 5
        return creditos.stream()
                .collect(Collectors.groupingBy(SolicitudCredito::getNivelMorosidad,
                        Collectors.counting()));
    }

    /**
     * Metodo que determina o calcula cual es la tasa de interes anual promedio que
     * se agregara a todos
     * los creditos que se encuentran en cartera
     * 
     * @return Valor promedio
     */
    public double tasaInteresPromedio() {
        return this.creditos.stream()
                .mapToDouble(SolicitudCredito::getTasaInteresAnual)
                .average()
                .orElse(0);
    }

    /**
     * Metodo que determinar cual es la tasa de interes maxima que se ha cobrado por
     * cada tipo de cliente
     * que se contiene en la cartera de creditos
     */
    public Map<String, Double> maximaTasaInteresPorTipo() {
        return this.creditos.stream()
                .collect(Collectors.groupingBy(
                        SolicitudCredito::getTipoCliente,
                        Collectors.collectingAndThen(
                                Collectors.mapping(
                                        SolicitudCredito::getTasaInteresAnual,
                                        Collectors.maxBy(Comparator.naturalOrder())),
                                optionalMax -> optionalMax.orElse(0.0))));

    }

    /**
     * Metodo que informa cual es la tasa promedio que se cobra, cual es la tasa
     * maxima de cada tipo de cliente
     * que se contiene y la cantidad de creditos cuya tasa de interes, en base a su
     * score financiero es mayor un 60%
     */
    public String informe() {
        String cadena = "Tasa Interes Promedio: %.2f | Maximos Tipo de Cliente: %s | Cantidad Cliente 60%%: %d | Conteo por Nivel Morosidad: %s";
        double ti = this.tasaInteresPromedio();
        int cantidad = this.filtrar(sc -> sc.getTasaInteresAnual() > 60).size();
        Map<String, String> tiposFormateados = maximaTasaInteresPorTipo().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.format("%.2f", e.getValue()),
                        (newKey, newValue) -> newKey,
                        LinkedHashMap::new));

        return cadena.formatted(ti, tiposFormateados, cantidad, conteoPorNivelMorosidad());

    }

}
