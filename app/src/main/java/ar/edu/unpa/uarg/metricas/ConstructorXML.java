package ar.edu.unpa.uarg.metricas;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Construye un documento XML a partir de las métricas que va recibiendo
 * mediante el método «adjuntarMetrica».
 * Esta clase fue desarrollada en base al trabajo de Juan Enriquez.
 * <p>
 * NOTA PARA EL DESARROLLADOR: El archivo XML en el que se almacenan las
 * métricas se guarda en el directorio raíz de la memoria del teléfono.
 * Lo puede ubicar con el nombre «metricas.xml».
 *
 * @author Ariel Machini
 */
public class ConstructorXML {

    /**
     * El nombre del archivo XML que se va a guardar (/‹NOMBRE AQUÍ›.xml).
     * El valor por defecto es «metricas».
     */
    public static String NOMBRE_ARCHIVO = "metricas";

    private static SimpleDateFormat formateadorFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);

    /**
     * Adjunta la métrica especificada por parámetros al archivo XML que
     * almacena las métricas.
     *
     * @param metrica El nombre de la métrica que se va a adjuntar. Por
     *                ejemplo, "latenciaMilisegundos".
     * @param valor   El valor correspondiente a la métrica especificada mediante
     *                la variable «metrica». Siguiendo con el ejemplo que se usó
     *                para el parámetro anterior, un ejemplo cualquiera de un
     *                valor apropiado sería «72» (milisegundos).
     * @author Ariel Machini
     */
    public static void adjuntarMetrica(String metrica, String valor) {
        File archivoXML = new File(Environment.getExternalStorageDirectory() + "/" + NOMBRE_ARCHIVO + ".xml");

        try {
            if (!archivoXML.exists()) {
                archivoXML.createNewFile();
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(archivoXML, true));

            long fechaSistema = System.currentTimeMillis();
            String fechaFormateada = formateadorFecha.format(fechaSistema);
            metrica = "<metrica nombre=\"" + metrica + "\" fecha=\"" + fechaFormateada + "\">" + valor + "</metrica>";

            bufferedWriter.append(metrica);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e("Error", "Se produjo un error de E/S durante la ejecución del método «adjuntarMetrica» de la clase ConstructorXML.");
        }
    }

    /**
     * Adjunta la métrica especificada por parámetros al archivo XML que
     * almacena las métricas.
     *
     * @param metrica    El nombre de la métrica que se va a adjuntar. Por
     *                   ejemplo, "latenciaMilisegundos".
     * @param valor      El valor correspondiente a la métrica especificada mediante
     *                   la variable «metrica». Siguiendo con el ejemplo que se usó
     *                   para el parámetro anterior, un ejemplo cualquiera de un
     *                   valor apropiado sería «72» (milisegundos).
     * @param comentario Un comentario definido por el desarrollador para la
     *                   métrica que se va a adjuntar.
     * @author Ariel Machini
     * @see #adjuntarMetrica(String, String)
     */
    public static void adjuntarMetrica(String metrica, String valor, String comentario) {
        File archivoXML = new File(Environment.getExternalStorageDirectory() + "/" + NOMBRE_ARCHIVO + ".xml");

        try {
            if (!archivoXML.exists()) {
                archivoXML.createNewFile();
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(archivoXML, true));

            long fechaSistema = System.currentTimeMillis();
            String fechaFormateada = formateadorFecha.format(fechaSistema);
            metrica = "<metrica nombre=\"" + metrica + "\" fecha=\"" + fechaFormateada + "\" comentario=\"" + comentario + "\">" + valor + "</metrica>";

            bufferedWriter.append(metrica);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e("Error", "Se produjo un error de E/S durante la ejecución del método «adjuntarMetrica» de la clase ConstructorXML.");
        }
    }

}
