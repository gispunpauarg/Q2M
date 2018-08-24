package ar.edu.unpa.uarg.metricas;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;

/**
 * Facilita el relevamiento de métricas de calidad de servicio (QoS) y calidad
 * de experiencia de usuario (QoE) a cualquier aplicación móvil para Android.
 *
 * @author Ariel Machini
 * @see #Metricas()
 * @see #Metricas(Context)
 */
public class Metricas implements android.hardware.SensorEventListener {

    private static Metricas instanciaMetricas = null;
    private Context contextoAplicacion;
    private float lux;
    private float proximidad;
    private long latenciaPercibidaUsuario;

    private Metricas() {
        this.latenciaPercibidaUsuario = Integer.MIN_VALUE;

        Log.w("Aviso", "Como no se tiene el contexto de la aplicación será imposible verificar que el usuario está dando los permisos necesarios a esta para el correcto funcionamiento de la librería.");
    }

    private Metricas(Context contexto) {
        this.contextoAplicacion = contexto;
        this.latenciaPercibidaUsuario = Integer.MIN_VALUE;
        SensorManager sensorManager = (SensorManager) this.contextoAplicacion.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorLux = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor sensorProximidad = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        sensorManager.registerListener(this, sensorLux, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorProximidad, SensorManager.SENSOR_DELAY_FASTEST);

        if (this.contextoAplicacion.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_DENIED) {
            Log.e("Error", "No se podrá actualizar el archivo «metricas.xml» en el teléfono porque el usuario no brindó el permiso necesario (READ_EXTERNAL_STORAGE).");
        }

        if (this.contextoAplicacion.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_DENIED) {
            Log.e("Error", "No se podrá crear el archivo «metricas.xml» en el teléfono porque el usuario no brindó el permiso necesario (WRITE_EXTERNAL_STORAGE).");
        }
    }


    /**
     * Destruye la instancia de la clase (<code>Metricas</code>) en el caso de
     * que exista.
     *
     * @author Ariel Machini
     * @see #getInstanceOf()
     * @see #getInstanceOf(Context)
     */
    public static void destroyInstance() {
        instanciaMetricas = null;
    }

    /**
     * Retorna una instancia de la clase <code>Metricas</code>.
     * Si ya se había creado una instancia con anterioridad, este método
     * simplemente retorna la instancia existente; por lo que si la primera
     * vez que pidió una instancia lo hizo con el método
     * <code>Metricas.getInstanceOf(Context)</code> se recomienda recuperar
     * dicha instancia con este método (<code>Metricas.getInstanceOf()</code>)
     * para su comodidad: Así no tendrá que proveer el contexto de la
     * aplicación cada vez que quiera recuperar la instancia de la clase.
     * Ahora, si es la primera vez que va a pedir una instancia de esta clase,
     * tenga en cuenta que si no provee el contexto de la aplicación no podrá
     * utilizar la mayor parte de las métricas, ya que requieren de dicho
     * parámetro para funcionar.
     *
     * @author Ariel Machini
     * @see #getInstanceOf(Context)
     */
    public static Metricas getInstanceOf() {
        if (instanciaMetricas == null) {
            instanciaMetricas = new Metricas();
        }

        return instanciaMetricas;
    }

    /**
     * Retorna una instancia de la clase <code>Metricas</code>.
     * Si ya se había creado una instancia con anterioridad, este método
     * simplemente retorna la instancia existente.
     *
     * @param contexto El contexto de la aplicación Android que va a hacer uso
     *                 de los servicios de la clase. Este es necesario para
     *                 obtener una serie de valores requeridos para generar los
     *                 resultados de ciertas métricas, tales como el brillo
     *                 actual de la pantalla y la luz del entorno en el que se
     *                 encuentra el usuario.
     * @author Ariel Machini
     */
    public static Metricas getInstanceOf(Context contexto) {
        if (instanciaMetricas == null) {
            instanciaMetricas = new Metricas(contexto);
        }

        return instanciaMetricas;
    }

    /* * * Acá comienzan los métodos heredados * * */

    /**
     * Este método no calcula ninguna métrica, y sólo tiene utilidad dentro de
     * la clase <code>Metricas</code>, donde se utiliza para calcular valores
     * de métricas que requieren del uso de sensores.
     */
    public void onAccuracyChanged(Sensor sensor, int precision) {
        /* Por el momento este método resulta inútil para las métricas, razón
         * por la cual se deja sin implementación. */
    }

    /**
     * Este método no calcula ninguna métrica, y sólo tiene utilidad dentro de
     * la clase <code>Metricas</code>, donde se utiliza para calcular valores
     * de métricas que requieren del uso de sensores.
     */
    public void onSensorChanged(android.hardware.SensorEvent evento) {
        if (evento.sensor.getType() == Sensor.TYPE_LIGHT) {
            this.lux = evento.values[0];

            Log.i("Cambio en el sensor", "Lux: " + this.lux + ".");
        } else if (evento.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            this.proximidad = evento.values[0];

            Log.i("Cambio en el sensor", "Proximidad: " + this.proximidad + ".");
        }
    }

    /* * * Acá terminan los métodos heredados * * */

    private String convertirFlujo(java.io.InputStream inputStream) {
        java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");

        return scanner.hasNext() ? scanner.next() : ""; // Retorna una cadena vacía si el flujo de entrada no provee ninguna información.
    }

    /**
     * (Métrica QoE) Devuelve el porcentaje de uso de la CPU que está
     * utilizando la aplicación.
     * NOTA: Para obtener el resultado de esta métrica se hace uso del proceso
     * <code>top</code>, razón por la cual el porcentaje obtenido refleja
     * cuánto del uso actual TOTAL de la CPU (considerado como el 100%) está
     * ocupando la aplicación. Si bien no es el mejor resultado, es lo más
     * preciso que se puede obtener.
     *
     * @return El porcentaje en uso de la CPU por la aplicación. Retorna -1 si
     * ocurre algún error durante la obtención de dicho porcentaje.
     * @author Ariel Machini
     */
    public double getAppCPUUsage() {
        if (this.contextoAplicacion != null) {
            ActivityManager activityManager = (ActivityManager) this.contextoAplicacion.getSystemService(Context.ACTIVITY_SERVICE);
            java.util.List<ActivityManager.RunningAppProcessInfo> listaProcesos = activityManager.getRunningAppProcesses();
            int PIDaplicacion = -1;

            for (ActivityManager.RunningAppProcessInfo procesoActual : listaProcesos) {
                if (procesoActual.processName.equalsIgnoreCase(contextoAplicacion.getPackageName())) {
                    PIDaplicacion = procesoActual.pid;

                    break;
                }
            }

            double usoCPU = Integer.MIN_VALUE;
            Process proceso = null;

            try {
                proceso = Runtime.getRuntime().exec("top -n 1 -p " + PIDaplicacion);
                int codigoSalida = proceso.waitFor();

                if (codigoSalida == 0) {
                    String salidaProceso = this.convertirFlujo(proceso.getInputStream());
                    String[] cadenaSalida = salidaProceso.split("\n");

                    for (int i = 0; i < cadenaSalida.length; i++) {
                        if (cadenaSalida[i].contains("%CPU")) {
                            /* ¡NO CAMBIAR LOS ÍNDICES YA ESTABLECIDOS! Estos
                             * fueron puestos así para capturar el porcentaje sin
                             * importar cuál sea su valor (es decir, comprende
                             * desde el 0.0 hasta el 100.0. */
                            usoCPU = Double.valueOf(cadenaSalida[i + 1].substring(42, 47).trim());

                            break;
                        }
                    }
                } else {
                    Log.e("Error", "Ocurrió un error durante la ejecución del proceso «getAppCPUUsage» (cód. " + codigoSalida + ").");

                    return -1;
                }
            } catch (IOException e) {
                Log.e("Error", "Se produjo un error de E/S durante la ejecución del método «getAppCPUUsage».");
            } catch (InterruptedException e) {
                Log.e("Error", "No se pudo terminar con la ejecución del método «getAppCPUUsage» porque esta fue interrumpida.");
            } finally {
                proceso.destroy();
            }

            ConstructorXML.adjuntarMetrica("applicationCPUUsage%", String.valueOf(usoCPU));

            return usoCPU;
        } else {
            Log.e("Error", "La métrica «applicationCPUUsage%» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoE) Retorna el porcentaje de carga actual de la batería del
     * teléfono.
     *
     * @return El porcentaje de carga (de 0% a 100%) del teléfono en el que se
     * está ejecutando la aplicación. Retorna -1 si no se puede obtener el
     * valor por alguna causa.
     * @author Ariel Machini
     */
    public int getBatteryPercentage() {
        if (this.contextoAplicacion != null) {
            android.content.IntentFilter intentFilter = new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent estadoBateria = this.contextoAplicacion.registerReceiver(null, intentFilter);

            int cargaActual = (estadoBateria.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) / estadoBateria.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            ConstructorXML.adjuntarMetrica("batteryCharge%", String.valueOf(cargaActual));

            return cargaActual;
        } else {
            Log.e("Error", "La métrica «getBatteryPercentage» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoE) Retorna el tipo de conexión que está utilizando el usuario
     * mientras utiliza la aplicación.
     *
     * @return El nombre del tipo de conexión que esté usando el usuario. Retorna
     * <code>null</code> si no se puede obtener el valor por alguna causa.
     * @author Ariel Machini
     */
    public String getConnectionType() {
        if (this.contextoAplicacion != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.contextoAplicacion.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
                ConstructorXML.adjuntarMetrica("connectionType", "Wi-Fi");

                return "Wi-Fi";
            } else if (connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE) {

                switch (connectivityManager.getActiveNetworkInfo().getSubtype()) {

                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        ConstructorXML.adjuntarMetrica("connectionType", "EDGE");
                        return "EDGE";

                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        ConstructorXML.adjuntarMetrica("connectionType", "GPRS");
                        return "GPRS";

                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        ConstructorXML.adjuntarMetrica("connectionType", "2G");
                        return "2G";

                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        ConstructorXML.adjuntarMetrica("connectionType", "3G");
                        return "3G";

                    case TelephonyManager.NETWORK_TYPE_LTE:
                        ConstructorXML.adjuntarMetrica("connectionType", "4G");
                        return "4G";

                    default: // Si el tipo de conexión no está contemplado arriba, se retorna igual.
                        String tipoConexion = connectivityManager.getActiveNetworkInfo().getSubtypeName();

                        ConstructorXML.adjuntarMetrica("connectionType", tipoConexion);

                        return tipoConexion;
                }

            } else {
                /* Si la conexión que está utilizando el usuario no coincide con
                 * ninguno de los tipos arriba especificados, entonces estamos
                 * ante un caso muy particular. De igual manera, se contempla la
                 * posibilidad por más mínima que sea agregando este último «else». */
                String tipoConexion = connectivityManager.getActiveNetworkInfo().getTypeName();

                ConstructorXML.adjuntarMetrica("connectionType", tipoConexion);

                return tipoConexion;
            }
        } else {
            Log.e("Error", "La métrica «getConnectionType» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return null;
        }
    }

    /**
     * (Métrica QoE) Mide la luz del entorno en el que se encuentra el usuario
     * y retorna el valor en lx (lux). Para aprender más sobre la unidad de
     * medida lux visite https://es.wikipedia.org/wiki/Lux.
     *
     * @return El nivel de luz del entorno en lx. Retorna -1 si no se puede
     * obtener el valor por alguna causa.
     * @author Ariel Machini
     */
    public float getEnvironmentLight() {
        if (this.contextoAplicacion != null) {
            ConstructorXML.adjuntarMetrica("environmentLightLx", String.valueOf(this.lux));

            return this.lux;
        } else {
            Log.e("Error", "La métrica «getEnvironmentLight» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoS) Calcula el getJitter en la comunicación a una dirección de IP
     * determinada y lo retorna.
     *
     * @param direccionIP La dirección de IP (IPv4 o IPv6) que se va a utilizar
     *                    para ejecutar la métrica.
     * @return El getJitter calculado. Retorna -1 si ocurre algún problema en
     * la comunicación entre el celular y el host especificado.
     * @author Ariel Machini
     */
    public double getJitter(String direccionIP) {
        if (this.contextoAplicacion != null) {
            if (isActiveNetworkInfoNotNull()) {
                java.util.ArrayList<Double> latencias = new java.util.ArrayList<>();
                Process proceso = null;

                try {
                    proceso = Runtime.getRuntime().exec("/system/bin/ping -c 4 " + direccionIP);
                    int codigoSalida = proceso.waitFor();

                    if (codigoSalida == 0) { // De acuerdo al API de Java, el código de salida 0 indica una ejecución exitosa.
                        String salidaProceso = this.convertirFlujo(proceso.getInputStream());
                        String[] cadenaSalida = salidaProceso.split("\n");

                        for (String linea : cadenaSalida) {
                            if (!linea.contains("time=")) {
                                continue;
                            }

                            int indice = linea.indexOf("time=") + 5; // El resultado de la métrica está después de los 5 caracteres de "time=".
                            double milisegundos = Double.valueOf(linea.substring(indice, linea.indexOf(" ms")));

                            latencias.add(milisegundos);
                        }
                    } else if (codigoSalida == 1) {
                        ConstructorXML.adjuntarMetrica("jitter", "Sin respuesta");

                        return -1;
                    } else {
                        Log.e("Error", "Ocurrió un error durante la ejecución del proceso «getJitter» (cód. " + codigoSalida + ").");

                        return -1;
                    }
                } catch (IOException e) {
                    Log.e("Error", "Se produjo un error de E/S durante la ejecución del método «getJitter».");
                } catch (InterruptedException e) {
                    Log.e("Error", "No se pudo terminar con la ejecución del método «getJitter» porque esta fue interrumpida.");
                } finally {
                    proceso.destroy();
                }

                try {
                    /* ((| T2 - T1 |) + (| T3 - T2 |) + (| T4 - T3 |)) / 3. Es una división
                     * entre tres porque se suman tres valores y se saca el promedio de esa
                     * suma. */
                    double jitter = (Math.abs(latencias.get(1) - latencias.get(0)) + Math.abs(latencias.get(2) - latencias.get(1)) + Math.abs(latencias.get(3) - latencias.get(2))) / 3;

                    ConstructorXML.adjuntarMetrica("jitter", String.valueOf(jitter));

                    return jitter;
                } catch (IndexOutOfBoundsException e) {
                    /* Una excepción de tipo IndexOutOfBoundsException implica que uno
                     * de los pings que se hizo a direccionIP resultó en timeout, razón
                     * por la cual se tendrá que volver a ejecutar el método. Esto es
                     * así porque, de otro modo, no se puede hacer la operación
                     * ((| T2 - T1 |) + (| T3 - T2 |) + (| T4 - T3 |)) / 3. */
                    return getJitter(direccionIP);
                }
            } else {
                Log.e("Error", "No se puede ejecutar el método «getJitter» porque el teléfono no está conectado a una red.");

                return -1;
            }
        } else {
            Log.e("Error", "La métrica «getJitter» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoS) Mide la latencia a una dirección de IP determinada.
     *
     * @param direccionIP La dirección de IP (IPv4 o IPv6) que se va a utilizar
     *                    para ejecutar la métrica.
     * @return La latencia en milisegundos. Retorna -1 si ocurre algún
     * problema en la comunicación entre el celular y el host especificado.
     * @author Ariel Machini
     */
    public double getLatency(String direccionIP) {
        if (this.contextoAplicacion != null) {
            if (isActiveNetworkInfoNotNull()) {
                double milisegundos = 0;
                Process proceso = null;

                try {
                    proceso = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + direccionIP);
                    int codigoSalida = proceso.waitFor();

                    if (codigoSalida == 0) {
                        String salidaProceso = this.convertirFlujo(proceso.getInputStream());
                        String[] cadenaSalida = salidaProceso.split("\n");

                        for (String linea : cadenaSalida) {
                            if (!linea.contains("time=")) {
                                continue;
                            }

                            int indice = linea.indexOf("time=") + 5; // El resultado de la métrica está después de los 5 caracteres de "time=".
                            milisegundos = Double.valueOf(linea.substring(indice, linea.indexOf(" ms")));

                            break;
                        }
                    } else if (codigoSalida == 1) {
                        ConstructorXML.adjuntarMetrica("latency", "Sin respuesta");

                        return -1;
                    } else {
                        Log.e("Error", "Ocurrió un error durante la ejecución del proceso «getLatency» (cód. " + codigoSalida + ").");

                        return -1;
                    }
                } catch (IOException e) {
                    Log.e("Error", "Se produjo un error de E/S durante la ejecución del método «getLatency».");
                } catch (InterruptedException e) {
                    Log.e("Error", "No se pudo terminar con la ejecución del método «getLatency» porque esta fue interrumpida.");
                } finally {
                    proceso.destroy();
                }

                ConstructorXML.adjuntarMetrica("latency", String.valueOf(milisegundos));

                return milisegundos;
            } else {
                Log.e("Error", "No se puede ejecutar el método «getLatency» porque el teléfono no está conectado a una red.");

                return -1;
            }
        } else {
            Log.e("Error", "La métrica «getLatency» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoS) Controla la pérdida de paquetes en la comunicación con una
     * dirección de IP determinada.
     * Cabe mencionar que para determinar la pérdida de paquetes se hace ping
     * cinco veces a la dirección de IP recibida por parámetros.
     *
     * @param direccionIP La dirección de IP (IPv4 o IPv6) que se va a utilizar
     *                    para ejecutar la métrica.
     * @return El porcentaje de paquetes perdidos. Retorna -1 si ocurre algún
     * problema en la comunicación entre el celular y el host especificado.
     * @author Ariel Machini
     */
    public int getPacketLoss(String direccionIP) {
        if (this.contextoAplicacion != null) {
            if (isActiveNetworkInfoNotNull()) {
                int paquetesPerdidos = 0;

                Process proceso = null;

                try {
                    proceso = Runtime.getRuntime().exec("/system/bin/ping -c 5 " + direccionIP);
                    int codigoSalida = proceso.waitFor();

                    if (codigoSalida == 0) {
                        String salidaProceso = this.convertirFlujo(proceso.getInputStream());
                        String[] cadenaSalida = salidaProceso.split("\n");

                        for (String linea : cadenaSalida) {
                            if (!linea.contains("packet loss")) {
                                continue;
                            }

                            int indiceFin = linea.indexOf("%");
                            paquetesPerdidos = Integer.valueOf(linea.substring(indiceFin - 3, indiceFin).replace(",", "").trim());

                            break;
                        }
                    } else if (codigoSalida == 1) {
                        paquetesPerdidos = 100;
                    } else {
                        Log.e("Error", "Ocurrió un error durante la ejecución del proceso «getPacketLoss» (cód. " + codigoSalida + ").");

                        return -1;
                    }
                } catch (IOException e) {
                    Log.e("Error", "Se produjo un error de E/S durante la ejecución del método «getPacketLoss».");
                } catch (InterruptedException e) {
                    Log.e("Error", "No se pudo terminar con la ejecución del método «getPacketLoss» porque esta fue interrumpida.");
                } finally {
                    proceso.destroy();
                }

                ConstructorXML.adjuntarMetrica("packetLoss%", String.valueOf(paquetesPerdidos));

                return paquetesPerdidos;
            } else {
                Log.e("Error", "No se puede ejecutar el método «getPacketLoss» porque el teléfono no está conectado a una red.");

                return -1;
            }
        } else {
            Log.e("Error", "La métrica «getPacketLoss» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoS) Controla la pérdida de paquetes en la comunicación con una
     * dirección de IP determinada.
     *
     * @param direccionIP        La dirección de IP (IPv4 o IPv6) que se va a utilizar
     *                           para ejecutar la métrica.
     * @param numeroRepeticiones Cantidad de veces que se va a hacer ping a la
     *                           dirección de IP recibida por parámetros.
     * @return El porcentaje de paquetes perdidos. Retorna -1 si ocurre algún
     * problema en la comunicación entre el celular y el host especificado.
     * @author Ariel Machini
     */
    public int getPacketLoss(String direccionIP, int numeroRepeticiones) {
        if (this.contextoAplicacion != null) {
            if (isActiveNetworkInfoNotNull()) {
                int paquetesPerdidos = 0;

                Process proceso = null;

                try {
                    proceso = Runtime.getRuntime().exec("/system/bin/ping -c " + numeroRepeticiones + " " + direccionIP);
                    int codigoSalida = proceso.waitFor();

                    if (codigoSalida == 0) {
                        String salidaProceso = this.convertirFlujo(proceso.getInputStream());
                        String[] cadenaSalida = salidaProceso.split("\n");

                        for (String linea : cadenaSalida) {
                            if (!linea.contains("packet loss")) {
                                continue;
                            }

                            int indiceFin = linea.indexOf("% packet loss");
                            paquetesPerdidos = Integer.valueOf(linea.substring(indiceFin - 3, indiceFin).replace(",", "").trim());

                            break;
                        }
                    } else if (codigoSalida == 1) {
                        paquetesPerdidos = 100;
                    } else {
                        Log.e("Error", "Ocurrió un error durante la ejecución del proceso «getPacketLoss» (cód. " + codigoSalida + ").");

                        return -1;
                    }
                } catch (IOException e) {
                    Log.e("Error", "Se produjo un error de E/S durante la ejecución del método «getPacketLoss».");
                } catch (InterruptedException e) {
                    Log.e("Error", "No se pudo terminar con la ejecución del método «getPacketLoss» porque esta fue interrumpida.");
                } finally {
                    proceso.destroy();
                }

                ConstructorXML.adjuntarMetrica("packetLoss%", String.valueOf(paquetesPerdidos));

                return paquetesPerdidos;
            } else {
                Log.e("Error", "No se puede ejecutar el método «getPacketLoss» porque el teléfono no está conectado a una red.");

                return -1;
            }
        } else {
            Log.e("Error", "La métrica «getPacketLoss» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoE) Mide y retorna la getProximity entre el dispositivo y el
     * usuario.
     *
     * @return La proximidad en centímetros. Retorna -1 si no se puede obtener
     * el valor por alguna causa.
     * @author Ariel Machini
     */
    public float getProximity() {
        if (this.contextoAplicacion != null) {
            ConstructorXML.adjuntarMetrica("proximityCentimeters", String.valueOf(this.proximidad));

            return this.proximidad;
        } else {
            Log.e("Error", "La métrica «getProximity» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoE) Retorna el porcentaje de brillo de la pantalla del
     * teléfono del usuario.
     *
     * @return El porcentaje de brillo (de 0% a 100%) de la pantalla del
     * teléfono a través de la cual se visualiza la aplicación. Retorna -1 si
     * no se puede obtener el valor por alguna causa.
     * @author Ariel Machini
     */
    public int getScreenBrightness() {
        if (this.contextoAplicacion != null) {
            int porcentajeBrillo = -1;

            try {
                porcentajeBrillo = android.provider.Settings.System.getInt(this.contextoAplicacion.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            } catch (android.provider.Settings.SettingNotFoundException e) {
                Log.e("Error", "Se produjo una excepción de tipo SettingNotFoundException dentro del método «getScreenBrightness».");
            }

            if (porcentajeBrillo != -1) {
                /* Para entender el porqué de esta cuenta, es necesario saber que
                 * Settings.System.SCREEN_BRIGHTNESS retorna el valor del brillo
                 * actual de la pantalla dentro de un rango que va de 0 a 255.
                 * FUENTE: https://developer.android.com/reference/android/provider/Settings.System.html#SCREEN_BRIGHTNESS
                 *
                 * Para poder devolver el porcentaje (que es lo que debe retornar
                 * el método), es necesario hacer la siguiente operación: */
                porcentajeBrillo = porcentajeBrillo * 100 / 255;

                ConstructorXML.adjuntarMetrica("screenBrightness%", String.valueOf(porcentajeBrillo));
            }

            return porcentajeBrillo;
        } else {
            Log.e("Error", "La métrica «getScreenBrightness» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoE) Retorna la intensidad de la señal en dBm.
     * NOTA: Si el valor que recibe de este método es 1, entonces el tipo de
     * conexión que está utilizando el usuario NO es inalámbrica.
     *
     * @return La potencia en dBm de la señal a la que el usuario está
     * conectado. Retorna 1 si no se puede obtener el valor por alguna causa
     * (esto es así porque el valor en dBm de la intensidad de la señal nunca
     * va a ser mayor que 0, por lo tanto 1 es un valor imposible).
     * @author Ariel Machini
     */
    public int getSignalStrength() {
        if (this.contextoAplicacion != null) {
            if (this.contextoAplicacion.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_DENIED) {
                ConnectivityManager connectivityManager = (ConnectivityManager) this.contextoAplicacion.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) this.contextoAplicacion.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int dBm = wifiManager.getConnectionInfo().getRssi();

                    ConstructorXML.adjuntarMetrica("signalStrengthdBm", String.valueOf(dBm));

                    return dBm;
                } else if (connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager telephonyManager = (TelephonyManager) this.contextoAplicacion.getSystemService(Context.TELEPHONY_SERVICE);
                    Object cellInfo = telephonyManager.getAllCellInfo().get(0);

                    if (cellInfo instanceof CellInfoLte) {
                        int dBm = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();

                        ConstructorXML.adjuntarMetrica("signalStrengthdBm", String.valueOf(dBm));

                        return dBm;
                    } else if (cellInfo instanceof CellInfoGsm) {
                        int dBm = ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();

                        ConstructorXML.adjuntarMetrica("signalStrengthdBm", String.valueOf(dBm));

                        return dBm;
                    } else if (cellInfo instanceof CellInfoCdma) {
                        int dBm = ((CellInfoCdma) cellInfo).getCellSignalStrength().getDbm();

                        ConstructorXML.adjuntarMetrica("signalStrengthdBm", String.valueOf(dBm));

                        return dBm;
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        int dBm = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();

                        ConstructorXML.adjuntarMetrica("signalStrengthdBm", String.valueOf(dBm));

                        return dBm;
                    } else {
                        Log.w("Intensidad de la señal", "Tipo de red inalámbrica desconocido. No es posible obtener sus dBm.");

                        return 1; // No debería entrar nunca a este «else», pero es necesario agregarlo.
                    }
                } else {
                    Log.e("Intensidad de la señal", "No está conectado a una red inalámbrica, por lo que la métrica no se guardará.");

                    return 1;
                }
            } else {
                Log.e("Error", "La métrica «getSignalStrength» no se puede ejecutar porque el usuario no brindó el permiso necesario (ACCESS_COARSE_LOCATION).");

                return 1;
            }
        } else {
            Log.e("Error", "La métrica «getSignalStrength» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return 1;
        }
    }

    /**
     * (Métrica QoE) Calcula y retorna la cantidad de MB de la memoria RAM del
     * teléfono que están en uso.
     *
     * @return La cantidad de MB en uso de la memoria RAM. Retorna -1 si no se
     * puede obtener el valor por alguna causa.
     * @author Ariel Machini
     */
    public double getTotalMemoryUsageMB() {
        if (this.contextoAplicacion != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) this.contextoAplicacion.getSystemService(Context.ACTIVITY_SERVICE);

            activityManager.getMemoryInfo(memoryInfo);

            double memoriaEnUso = (memoryInfo.totalMem / 0x100000L) - (memoryInfo.availMem / 0x100000L);

            ConstructorXML.adjuntarMetrica("memoryUsedMB", String.valueOf(memoriaEnUso));

            return memoriaEnUso;
        } else {
            Log.e("Error", "La métrica «getTotalMemoryUsageMB» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoE) Calcula y retorna el porcentaje de la memoria RAM del
     * teléfono que está en uso.
     *
     * @return El porcentaje en uso de la memoria RAM. Retorna -1 si no se
     * puede obtener el valor por alguna causa.
     * @author Ariel Machini
     */
    public double getTotalMemoryUsagePercentage() {
        if (this.contextoAplicacion != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) this.contextoAplicacion.getSystemService(Context.ACTIVITY_SERVICE);

            activityManager.getMemoryInfo(memoryInfo);

            double memoriaEnUso = (memoryInfo.totalMem / 0x100000L) - (memoryInfo.availMem / 0x100000L);
            double porcentajeEnUso = memoriaEnUso * 100 / (memoryInfo.totalMem / 0x100000L);

            ConstructorXML.adjuntarMetrica("memoryUsed%", String.valueOf(porcentajeEnUso));

            return porcentajeEnUso;
        } else {
            Log.e("Error", "La métrica «getTotalMemoryUsagePercentage» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return -1;
        }
    }

    /**
     * (Métrica QoE) Reporta si el teléfono está cargando (ya sea por conexión
     * CA, USB o inalámbricamente) o no.
     *
     * @return <code>true</code> si el teléfono está cargando y
     * <code>false</code> en caso contrario.
     * @author Ariel Machini
     */
    public boolean isPhoneCharging() {
        if (this.contextoAplicacion != null) {
            android.content.IntentFilter intentFilter = new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent estadoBateria = this.contextoAplicacion.registerReceiver(null, intentFilter);
            int tipoConexion = estadoBateria.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            boolean estaConectado = (tipoConexion == BatteryManager.BATTERY_PLUGGED_AC || tipoConexion == BatteryManager.BATTERY_PLUGGED_USB || tipoConexion == BatteryManager.BATTERY_PLUGGED_WIRELESS);

            ConstructorXML.adjuntarMetrica("phoneCharging", String.valueOf(estaConectado));

            return estaConectado;
        } else {
            Log.e("Error", "La métrica «isPhoneCharging» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return false;
        }
    }

    /**
     * (Métrica QoE) Verifica que el teléfono esté conectado
     * (independientemente si tiene conexión a internet o no) a una red.
     *
     * @return <code>true</code> si el teléfono está conectado a una red y
     * <code>false</code> en caso contrario.
     * @author Ariel Machini
     */
    public boolean isPhoneConnected() {
        if (this.contextoAplicacion != null) {
            boolean isPhoneConnected = this.isActiveNetworkInfoNotNull();

            ConstructorXML.adjuntarMetrica("phoneConnectedToANetwork", String.valueOf(isPhoneConnected));

            return isPhoneConnected;
        } else {
            Log.e("Error", "La métrica «isPhoneConnected» no puede retornar un valor porque no brindó el contexto de la aplicación al constructor.");

            return false;
        }
    }

    private boolean isActiveNetworkInfoNotNull() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.contextoAplicacion.getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null;
    }

    /**
     * Comienza a medir la latencia percibida por el usuario. Este método debe
     * llamarse antes de la ejecución de la operación o del conjunto de
     * operaciones que se va a medir. Nótese que no se obtendrá ningún
     * resultado hasta que se llame el método <code>perceivedLatencyStop()</code>.
     *
     * @author Ariel Machini
     * @see #perceivedLatencyStop()
     */
    public void perceivedLatencyBegin() {
        this.latenciaPercibidaUsuario = System.currentTimeMillis();
    }

    /**
     * (Métrica QoS) Finaliza la medición de la latencia percibida por el
     * usuario. Este método debe llamarse después de la ejecución de la
     * operación o del conjunto de operaciones que se va a medir, siempre y
     * cuando se haya llamado a <code>perceivedLatencyBegin()</code> primero.
     * Retorna la latencia percibida por el usuario en segundos.
     *
     * @return La latencia percibida por el usuario en segundos. Retorna
     * -1 si no se llamó al método <code>perceivedLatencyBegin()</code>
     * primero.
     * @author Ariel Machini
     * @see #perceivedLatencyBegin()
     */
    public long perceivedLatencyStop() {
        if (this.latenciaPercibidaUsuario != Integer.MIN_VALUE) {
            long latenciaPercibidaFinal = (System.currentTimeMillis() - this.latenciaPercibidaUsuario) / 1000;

            ConstructorXML.adjuntarMetrica("perceivedLatencySeconds", String.valueOf(latenciaPercibidaFinal));

            /* Se restaura el valor de esta variable para que en posteriores
             * ejecuciones de la misma métrica no se pueda llamar a este
             * método sin antes llamar a perceivedLatencyBegin(). */
            this.latenciaPercibidaUsuario = Integer.MIN_VALUE;

            return latenciaPercibidaFinal;
        } else {
            Log.e("Latencia percibida", "Para poder usar el método perceivedLatencyStop() primero debe utilizar el método perceivedLatencyBegin().");

            return -1;
        }
    }

}
