import com.sun.istack.internal.NotNull;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Uso {
    int numUso, duracionEstaciones, primeraEstacion; //PrimeraEstacion: 0 Otoño, 1 Primavera, 2 cualquiera
    float productividadTotal;
    float fosforo;
    float[] productividad, fosforoEstacion;
    List<Integer> siguientesUsos;
    String nombre;
    Date fechaAlta;
    boolean archivado = false;

    public Uso() {

    }

/**Constructor de la clase**/
    public Uso(int nUso, int dEstaciones, int primeraEstacion, float fosforo, float prod[], float fosforoEstacion[], String nombre, List<Integer> sigUsos) {
        this.numUso = nUso;
        this.duracionEstaciones = dEstaciones;
        this.primeraEstacion = primeraEstacion;
        this.fosforoEstacion = fosforoEstacion;
        this.fosforo = fosforo;
        this.productividad = prod;
        this.productividadTotal = 0;
        for (int iEstacion = 0; iEstacion < dEstaciones; iEstacion++) {
            this.productividadTotal += this.productividad[iEstacion];
        }

        this.nombre = nombre;
        this.siguientesUsos = sigUsos;
        this.fechaAlta = new Date();
    }

    public static Uso emptyUso(List<Uso> usos) { // valores por defecto al crear un nuevo uso
        Uso res = new Uso();
        res.numUso = Uso.getNumeroUso(usos);
        res.nombre = "";
        res.duracionEstaciones = 16;
        res.primeraEstacion = ConstantesUI.OTONIO;
        res.fosforoEstacion = new float[res.duracionEstaciones];
        res.productividad = new float[res.duracionEstaciones];
        res.fosforo = 0;
        res.productividadTotal = 0;
        res.siguientesUsos = new ArrayList<>();
        return res;
    }

    public static String[] getAtributosFromTable() {
        return new String[] {"Número", "Nombre", "Id Ext", "Duracion", "Creación", "Ampliar", "Duplicar", "Borrar"};
    }

    public static int cantUsos(List<Uso> uso, boolean showArchivados) {
        if (showArchivados) return uso.size();
        int size = 0;
        for(Uso u: uso) {
            if (!u.archivado) {
                size++;
            }
        }
        return size;
    }

    public static  String[] getDatosSiguientesUsos(List<Uso> usos) {
        String[] lista = new String[usos.size()];
        for (int i = 0; i < usos.size(); i++) {
            lista[i] = usos.get(i).toStringArraySigUso();
        }
        return lista;
    }

    public void print() {
        System.out.println("Uso : "+nombre+", nombre: "+ nombre + ", duracion: "+duracionEstaciones+", primer estacion "+primeraEstacion);
        System.out.print("fosforo estacion: ");
        for (float aFosforoEstacion : fosforoEstacion) System.out.print(aFosforoEstacion + ", ");
        System.out.println();
        System.out.print("prod estacion: ");
        for (float aProductividad : productividad) System.out.print(aProductividad + ", ");
        System.out.println();
        System.out.print("siguientes usos: ");
        for (Integer siguientesUso : siguientesUsos) System.out.print(siguientesUso + ", ");
        System.out.println();
    }

    public static int getNumeroUso(List<Uso> usos) {
        int num = 0;
        int iter = 0;
        while (iter < usos.size()) {
            if (usos.get(iter).numUso == num) {
                num++;
                iter = 0;
            } else {
                iter++;
            }
        }
        return num;
    }

    /* Copia un uso, dandole un nuevo numero, y asignandole el nombre pasado como parametro */
    public Uso copy(List<Uso> usos, String nombre) {
        int num = Uso.getNumeroUso(usos);
        ArrayList<Integer> copy = new ArrayList<>(siguientesUsos);
        float[] prod = productividad.clone();
        float[] fos = fosforoEstacion.clone();
        return new Uso(num, duracionEstaciones, primeraEstacion, fosforo, prod, fos, nombre, copy);
    }

    /* Clona un uso, mismo id, mismo nombre, etc */
    public Uso clone() {
        ArrayList<Integer> copy = new ArrayList<>(siguientesUsos);
        float[] prod = productividad.clone();
        float[] fos = fosforoEstacion.clone();
        return new Uso(numUso, duracionEstaciones, primeraEstacion, fosforo, prod, fos, nombre, copy);
    }

    public static List<Uso> cloneList(List<Uso> lista) {
        List<Uso> result = new ArrayList<>();
        for (Uso uso: lista) {
            result.add(uso.clone());
        }
        return result;
    }

    /* Retorna los indices de los usos que aparecen en la lista seleccionados, respecto a la lista todos*/
    public static List<Integer> getIndexesFromListUsos(List<Uso> todos, @NotNull List<Uso> seleccionados) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < seleccionados.size(); i++) {
            int index = Uso.getIndex(todos, seleccionados.get(i).numUso);
            indexes.add(index);
        }
        return indexes;
    }

    public static List<Integer> getIndexesFromListNumbers(List<Uso> todos, List<Integer> seleccionados) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < seleccionados.size(); i++) {
            int index = Uso.getIndex(todos, seleccionados.get(i));
            indexes.add(index);
        }
        return indexes;
    }

    public String getNombreCopia() {
        return "copia de " + nombre;
    }
/*
    public Object[] toObjectArray(List<Problema> problemas, List<Resultado> resultados) {
        String borrar = "Borrar";
        if (archivado) {
            borrar = "Desarchivar";
        } else if (!Problema.getProblemasWithUso(problemas, numUso).equals("") || !Resultado.getResultadosWithUso(resultados, numUso).equals("")) {
            borrar = "Archivar";
        }
        return new Object[]{numUso, nombre, nombre, duracionEstaciones, Utils.getFormatedDate(fechaAlta), "Ampliar", "Duplicar", borrar};
    }
*/
    public String toStringArraySigUso() {
        return nombre + " (" + numUso + ") (" + nombre + ")";
    }

    public String getTitulo() {
        return "Uso " + nombre + ", (" + numUso + ")";
    }

    public static Uso getUsoByIndex(List<Uso> usos, int index, boolean mostrarArchivados) {
        int iter = 0;
        int archivados = 0;
        while(iter < usos.size()) {
            if (!mostrarArchivados && usos.get(iter).archivado) {
                iter++;
                archivados++;
            } else if (iter == index + archivados) {
                return usos.get(iter);
            } else {
                iter++;
            }
        }
        return null;
    }

    public static int getIndex(List<Uso> usos, int numUso) {
        int iter = 0;
        while(iter < usos.size()) {
            if (usos.get(iter).numUso == numUso) {
                return iter;
            } else {
                iter++;
            }
        }
        return -1;
    }

    public void agregarSiguienteUso(int numUso) {
        int iter = 0;
        while (iter < siguientesUsos.size()) {
            if (siguientesUsos.get(iter) == numUso) {
                return;
            } else {
                iter++;
            }
        }
        siguientesUsos.add(numUso);
    }

    public void quitarSiguienteUso(int numUso) {
        int iter = 0;
        while (iter < siguientesUsos.size()) {
            if (siguientesUsos.get(iter) == numUso) {
                siguientesUsos.remove(iter);
            } else {
                iter++;
            }
        }
    }
    
    /**Devuelve un siguiente uso aleatorio que pueda seguir al usoOriginal**/
    public static int siguienteUsoUniforme(int usoOriginal) {
        //Devuelve la posicion en el array de usos del siguiente uso
        //System.out.println("\t\tCantidad de Posibles siguientes usos: "+Constantes.usos[usoOriginal].siguientesUsos.size());
        int numeroUniforme = 0;  //En caso de tener solo un siguiente uso selecciono ese
        if (Constantes.usos[usoOriginal].siguientesUsos.size() > 1) { //En caso de que tenga mas de un uso
            numeroUniforme = Constantes.uniforme.nextInt(Constantes.usos[usoOriginal].siguientesUsos.size() - 1); // elijo uno uniforme entre cero y la cantidad de siguientes usos -1
        }
        //System.out.println("\t\tSiguiente Uso: "+numeroUniforme);
        int siguienteUso = Constantes.usos[usoOriginal].siguientesUsos.get(numeroUniforme);//Posicion en el array coincide con el numero de Uso
        return siguienteUso;
    }

    /**Devuelve un siguiente uso aleatorio, priorizando los que exportan menos fosforo, que pueda seguir al usoOriginal**/
    public static int siguienteUsoRuletaFosforo(int usoOriginal) {
        //Devuelve la posicion en el array de usos del siguiente uso
        //System.out.println("\t\tCantidad de Posibles siguientes usos: "+Constantes.usos[usoOriginal].siguientesUsos.size());
        boolean encontre = false;
        int siguienteUso = 0;
        float fosforoMaximo = 0, fosforoSorteado = 0, fosforoAcumulado = 0;

        //Calculo el maximo fosforo entre los que sortear
        for (int iUsoSiguiente = 0; iUsoSiguiente < Constantes.usos[usoOriginal].siguientesUsos.size(); iUsoSiguiente++) {
            fosforoMaximo += Constantes.usos[iUsoSiguiente].fosforo;
        }
        //En caso de que tenga mas de un uso sorteo un valor entre el fosforo maximo
        if (Constantes.usos[usoOriginal].siguientesUsos.size() > 1) {
            fosforoSorteado = Constantes.uniforme.nextFloat() * fosforoMaximo; // elijo uno uniforme entre cero y el fosforo maximo
        }
        //Veo a que uso corresponde el fosforo
        for (int iUsoSiguiente = 0; iUsoSiguiente < Constantes.usos[usoOriginal].siguientesUsos.size() && !encontre; iUsoSiguiente++) {
            //Sumo el fosforo del uso actual al acumulado
            fosforoAcumulado += Constantes.usos[iUsoSiguiente].fosforo;
            //Chequeo si el fosforo sorteado pertenece al uso actual
            if (fosforoSorteado < fosforoAcumulado) {
                siguienteUso = Constantes.usos[usoOriginal].siguientesUsos.get(iUsoSiguiente);//Posicion en el array coincide con el numero de Uso
                encontre = true;
            }
        }
        //System.out.println("\t\tSiguiente Uso: "+numeroUniforme);
        return siguienteUso;
    }

    /**Devuelve un siguiente uso aleatorio, priorizando los que tengan mayor produccion, que pueda seguir al uso Original**/
    public static int siguienteUsoRuletaProduccion(int usoOriginal) {
        //Devuelve la posicion en el array de usos del siguiente uso
        //System.out.println("\t\tCantidad de Posibles siguientes usos: "+Constantes.usos[usoOriginal].siguientesUsos.size());
        boolean encontre = false;
        int siguienteUso = 0;
        float produccionMaxima = 0, produccionSorteada = 0, produccionAcumulada = 0;

        //Calculo la maxima produccion entre los que sortear
        for (int iUsoSiguiente = 0; iUsoSiguiente < Constantes.usos[usoOriginal].siguientesUsos.size(); iUsoSiguiente++) {
            produccionMaxima += Constantes.usos[iUsoSiguiente].productividadTotal;
        }
        //En caso de que tenga mas de un uso sorteo un valor entre la productividad maxima
        if (Constantes.usos[usoOriginal].siguientesUsos.size() > 1) {
            produccionSorteada = Constantes.uniforme.nextFloat() * produccionMaxima; // elijo uno uniforme entre cero y el fosforo maximo
        }
        //Veo a que uso corresponde el fosforo
        for (int iUsoSiguiente = 0; iUsoSiguiente < Constantes.usos[usoOriginal].siguientesUsos.size() && !encontre; iUsoSiguiente++) {
            //Sumo la productividad actual al acumulado
            produccionAcumulada += Constantes.usos[iUsoSiguiente].productividadTotal;
            //Chequeo si la productividad sorteado pertenece al uso actual
            if (produccionSorteada < produccionAcumulada) {
                siguienteUso = Constantes.usos[usoOriginal].siguientesUsos.get(iUsoSiguiente);//Posicion en el array coincide con el numero de Uso
                encontre = true;
            }
        }
        //System.out.println("\t\tSiguiente Uso: "+numeroUniforme);
        return siguienteUso;
    }

    /**Devuelve un siguiente uso aleatorio, sorteando por Fosforo e intentando no Incumplir con la restriccion de Cantusos**/
    public static int siguienteUsoRuletaFosforoCumpleCantUsos(int usoOriginal, @NotNull ArrayList<Integer> usosDelProductorEstaEstacion, int iProductor) {
        List<Integer> listaDeCandidatos = new ArrayList<>();
        //Dado un uso y una lista de usos ya usados, devuelvo un uso sorteado ruleta por fosforo  que respete la restriccion de usos
        if (usosDelProductorEstaEstacion.size() < Constantes.productores[iProductor].getMinCantUsos()) {
            // ruleta con usos siguientes fuera de la lista
            for (Integer iUso : Constantes.usos[usoOriginal].siguientesUsos) {
                if (!usosDelProductorEstaEstacion.contains(iUso)) {
                    listaDeCandidatos.add(iUso);
                }
            }

        } else if (usosDelProductorEstaEstacion.size() < Constantes.maximaCantidadUsos) {
            // ruleta con todos los usus siguientes
            listaDeCandidatos = Constantes.usos[usoOriginal].siguientesUsos;
        } else {
            // ruleta con los usos siguientes presentes en la lista
            for (Integer iUso : Constantes.usos[usoOriginal].siguientesUsos) {
                if (usosDelProductorEstaEstacion.contains(iUso) && listaDeCandidatos.size() <= Constantes.maximaCantidadUsos) {
                    listaDeCandidatos.add(iUso);
                }
            }
        }
        if (listaDeCandidatos.size() == 1) {
            return listaDeCandidatos.get(0);
        } else {
            //System.out.println("\t\tCantidad de Posibles siguientes usos: "+Constantes.usos[usoOriginal].siguientesUsos.size());
            boolean encontre = false;
            int siguienteUso = 0;
            float fosforoMaximo = 0, fosforoSorteado = 0, fosforoAcumulado = 0;
            //Calculo el maximo fosforo entre los que sortear
            for (int iUsoSiguiente = 0; iUsoSiguiente < listaDeCandidatos.size(); iUsoSiguiente++) {
                fosforoMaximo += Constantes.usos[iUsoSiguiente].fosforo;
            }
            //En caso de que tenga mas de un uso sorteo un valor entre el fosforo maximo
            fosforoSorteado = Constantes.uniforme.nextFloat() * fosforoMaximo; // elijo uno uniforme entre cero y el fosforo maximo
            //Veo a que uso corresponde el fosforo
            for (int iUsoSiguiente = 0; iUsoSiguiente < listaDeCandidatos.size() && !encontre; iUsoSiguiente++) {
                //Sumo el fosforo del uso actual al acumulado
                fosforoAcumulado += Constantes.usos[iUsoSiguiente].fosforo;
                //Chequeo si el fosforo sorteado pertenece al uso actual
                if (fosforoSorteado < fosforoAcumulado) {
                    siguienteUso = Constantes.usos[usoOriginal].siguientesUsos.get(iUsoSiguiente);//Posicion en el array coincide con el numero de Uso
                    encontre = true;
                }
            }
            //System.out.println("\t\tSiguiente Uso: "+numeroUniforme);
            return siguienteUso;
        }
    }

    /**Devuelve un siguiente uso aleatorio, sorteando por Profuctividad e intentando no Incumplir con la restriccion de Cantusos**/
    public static int siguienteUsoRuletaProduccionCumpleCantUsos(int usoOriginal, @NotNull ArrayList<Integer> usosDelProductorEstaEstacion, int iProductor) {
        List<Integer> listaDeCandidatos = new ArrayList<>();
        //Dado un uso y una lista de usos ya usados, devuelvo un uso sorteado ruleta por fosforo  que respete la restriccion de usos
        if (usosDelProductorEstaEstacion.size() < Constantes.productores[iProductor].getMinCantUsos()) {
            // ruleta con usos siguientes fuera de la lista
            for (Integer iUso : Constantes.usos[usoOriginal].siguientesUsos) {
                if (!usosDelProductorEstaEstacion.contains(iUso)) {
                    listaDeCandidatos.add(iUso);
                }
            }

        } else if (usosDelProductorEstaEstacion.size() < Constantes.maximaCantidadUsos) {
            // ruleta con todos los usus siguientes
            listaDeCandidatos = Constantes.usos[usoOriginal].siguientesUsos;
        } else {
            // ruleta con los usos siguientes presentes en la lista
            for (Integer iUso : Constantes.usos[usoOriginal].siguientesUsos) {
                if (usosDelProductorEstaEstacion.contains(iUso) && listaDeCandidatos.size() <= Constantes.maximaCantidadUsos) {
                    listaDeCandidatos.add(iUso);
                }
            }
        }
        if (listaDeCandidatos.size() == 1) {
            return listaDeCandidatos.get(0);
        } else {
            //System.out.println("\t\tCantidad de Posibles siguientes usos: "+Constantes.usos[usoOriginal].siguientesUsos.size());
            boolean encontre = false;
            int siguienteUso = 0;
            float productividadMaxima = 0, productividadSorteado = 0, productividadAcumulado = 0;
            //Calculo el maximo fosforo entre los que sortear
            for (int iUsoSiguiente = 0; iUsoSiguiente < listaDeCandidatos.size(); iUsoSiguiente++) {
                productividadMaxima += Constantes.usos[iUsoSiguiente].fosforo;
            }
            //En caso de que tenga mas de un uso sorteo un valor entre el fosforo maximo
            productividadSorteado = Constantes.uniforme.nextFloat() * productividadMaxima; // elijo uno uniforme entre cero y el fosforo maximo
            //Veo a que uso corresponde el fosforo
            for (int iUsoSiguiente = 0; iUsoSiguiente < listaDeCandidatos.size() && !encontre; iUsoSiguiente++) {
                //Sumo el fosforo del uso actual al acumulado
                productividadAcumulado += Constantes.usos[iUsoSiguiente].fosforo;
                //Chequeo si el fosforo sorteado pertenece al uso actual
                if (productividadSorteado < productividadAcumulado) {
                    siguienteUso = Constantes.usos[usoOriginal].siguientesUsos.get(iUsoSiguiente);//Posicion en el array coincide con el numero de Uso
                    encontre = true;
                }
            }
            //System.out.println("\t\tSiguiente Uso: "+numeroUniforme);
            return siguienteUso;
        }
    }

    /**Carga los usos desde archivos uso[NumeroUso].in**/
    public static void cargarUsosDesdeArchivos() {
        //System.out.println("CantUsos: "+Constantes.cantUsos);
        Constantes.usos = new Uso[Constantes.cantUsos];
        Constantes.usos[0] = Uso.obtenerUsoReservado();
        System.out.print("Cargando uso : ");
        for (int i = 1; i < Constantes.cantUsos; i++) {
            System.out.print(" " + i);
            Uso.cargarUsoDesdeArchivo(i, "uso" + String.valueOf(i) + ".in");
        }
        System.out.println();
    }

    //TODO: Crear copia para json
    //TODO: Programacion Defensiva
    /**Martin**/
    private static void cargarUsoDesdeArchivo(int indice, String nombreArchivo) {
        String nombre = "Sin nombre";
        int numeroUso = 0, duracion = 0, primeraEstacion = 0;
        Float fosforo = 0.F;
        float[] productividad = new float[0], fosforoEstacion = new float[0];
        List<Integer> siguientesUsos = new ArrayList<>();
        String[] campos, elementos;
        // This will reference one line at a time
        String line = null;
        Boolean hayNombre = false, hayNumeroUso = false, hayDuracion = false, hayPrimeraEstacion = false, hayFosforo = false, hayProductividad = false, hayFosforos = false, haySigUsos = false;
        //Abrir Archivo
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(nombreArchivo);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            //Leo las lineas
            while ((line = bufferedReader.readLine()) != null) {
                //System.out.println("Linea "+iPixel+":"+line);
                //Parceo la linea segun sus separadores
                campos = line.split("=");
                if (campos.length > 0) {
                    //Filtro segun la etiqueta
                    switch (campos[0]) {
                        case "Nombre ":
                            //System.out.print("campos[1]:" +campos[1]+"\t");
                            Pattern pattern = Pattern.compile("\\w.*\\w");
                            Matcher m = pattern.matcher(campos[1]);
                            //System.out.print("m.find():" +m.find() +"\t");
                            //System.out.print("m.start():" +m.start()+"\t");
                            m.find();
                            nombre = m.group(0);
                            //System.out.println("nombre:"+nombre);
                            hayNombre = true;
                            break;
                        case "NumeroUso ":
                            numeroUso = Integer.parseInt(campos[1].replaceAll("\\s+", ""));
                            hayNumeroUso = true;
                            break;
                        case "Duracion ":
                            duracion = Integer.valueOf(campos[1].replaceAll("\\s+", ""));
                            hayDuracion = true;
                            break;
                        case "Primera Estacion ":
                            primeraEstacion = Integer.valueOf(campos[1].replaceAll("\\s+", ""));
                            hayPrimeraEstacion = true;
                            break;
                        case "Fosforo ":
                            fosforo = Float.valueOf(campos[1].replaceAll("\\s+", ""));
                            hayFosforo = true;
                            break;
                        case "Productividades ":
                            //System.out.println("duracion: "+duracion);
                            //System.out.println("campos[1]: "+campos[1]);
                            productividad = new float[duracion];
                            //Separo la lista por sus espacios
                            elementos = campos[1].split(" ");
                            //Cargo cada elemento en la lista
                            for (int iElemento = 1; iElemento <= duracion; iElemento++) {
                                //System.out.println("elemento["+iElemento+"]: "+elementos[iElemento]);

                                productividad[iElemento - 1] = Float.valueOf(elementos[iElemento]);
                            }
                            hayProductividad = true;
                            break;
                        case "Fosforos ":
                            fosforoEstacion = new float[duracion];
                            //Separo la lista por sus espacios
                            elementos = campos[1].split(" ");
                            //Cargo cada elemento en la lista
                            for (int iElemento = 1; iElemento <= duracion; iElemento++) {
                                fosforoEstacion[iElemento - 1] = Float.valueOf(elementos[iElemento]);
                            }
                            hayFosforos = true;
                            break;
                        case "Siguientes Usos ":
                            //System.out.println("duracion: "+duracion);
                            //System.out.println("campos[1]: "+campos[1]);
                            //Separo la lista por sus espacios
                            elementos = campos[1].split(" ");
                            //Cargo cada elemento en la lista
                            for (int iElemento = 1; iElemento < elementos.length; iElemento++) {
                                //System.out.println("elemento["+iElemento+"]: "+elementos[iElemento]);
                                siguientesUsos.add(Integer.valueOf(elementos[iElemento]));
                            }
                            haySigUsos = true;
                            break;
                    }
                }
            }
            if (!hayNombre) {
                System.out.println("Problemas con el nombre del uso del archivo: " + nombreArchivo);
            }
            if (!hayNumeroUso) {
                System.out.println("Problemas con el numero de uso del archivo: " + nombreArchivo);
            }
            if (!hayDuracion) {
                System.out.println("Problemas con la duracion del uso del archivo: " + nombreArchivo);
            }
            if (!hayPrimeraEstacion) {
                System.out.println("Problemas con la primera estacion del uso del archivo: " + nombreArchivo);
            }
            if (!hayFosforo) {
                System.out.println("Problemas con el fosforo del uso del archivo: " + nombreArchivo);
            }
            if (!hayProductividad) {
                System.out.println("Problemas con la productividad por estacion del uso del archivo: " + nombreArchivo);
            }
            if (!hayFosforos) {
                System.out.println("Problemas con el fosforo por estacion del uso del archivo: " + nombreArchivo);
            }
            if (!haySigUsos) {
                System.out.println("Problemas con los siguientes usos del uso del archivo: " + nombreArchivo);
            }

            if (hayNombre && hayNumeroUso && hayDuracion && hayPrimeraEstacion && hayFosforo && hayProductividad && hayFosforos && haySigUsos) {
                Constantes.usos[indice] = new Uso(numeroUso, duracion, primeraEstacion, fosforo, productividad, fosforoEstacion, nombre, siguientesUsos);

            }


            // Always close files.
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "CargarPixel: Unable to open file '" +
                            nombreArchivo + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + nombreArchivo + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }


        //Parsear datos
        //Ingresar datos
    }
    /**Imprime la informacion del Uso**/
    public void imprimirUso() {
        System.out.printf("\t(" + this.nombre + " , " + this.numUso + " , " + this.duracionEstaciones + " , " + this.primeraEstacion + " , " + this.fosforo + " , {");
        //Imprimo todos los elementos de productividad
        for (int i = 0; i < this.productividad.length; i++) {
            if (i != 0) {
                System.out.printf(",");
            }
            System.out.printf(String.valueOf(this.productividad[i]));
        }
        System.out.printf("},{");

        for (int i = 0; i < this.fosforoEstacion.length; i++) {
            if (i != 0) {
                System.out.printf(",");
            }
            System.out.printf(String.valueOf(this.fosforoEstacion[i]));
        }
        System.out.println("},");
        System.out.printf("\t\t{");

        for (int i = 0; i < this.siguientesUsos.size(); i++) {
            if (i != 0) {
                System.out.printf(",");
            }
            System.out.printf(String.valueOf(this.siguientesUsos.get(i)));
        }
        System.out.println("})");
    }
    /**Imprime informacion sobre todos los usos**/
    public static void imprimirUsos() {
        for (int i = 0; i < Constantes.usos.length; i++) {
            Constantes.usos[i].imprimirUso();
        }
    }
    /**Crea archivos para cada uno de los usos uso[NumeroUso].in**/
    public static void crearArchivosUsos() {
        for (int i = 0; i < Constantes.usos.length; i++) {
            Constantes.usos[i].crearArchivo(String.valueOf(i));
        }
    }

    //TODO: Crear copia para json
    /**Martin**/
    private void crearArchivo(String numeroUso) {
        try {
            //Creo el archivo
            PrintWriter archivo = new PrintWriter(new FileOutputStream(new File("uso" + numeroUso + ".in"), false /* append = true */));
            archivo.println("Nombre = " + this.nombre);
            archivo.println("NumeroUso = " + this.numUso);
            archivo.println("Duracion = " + this.duracionEstaciones);
            archivo.println("Primera Estacion = " + this.primeraEstacion);
            archivo.println("Fosforo = " + this.fosforo);
            archivo.print("Productividades = ");
            for (int i = 0; i < this.productividad.length; i++) {
                if (i != 0) {
                    archivo.printf(" ");
                }
                archivo.printf(String.valueOf(this.productividad[i]));
            }
            archivo.println("");
            archivo.print("Fosforos = ");
            for (int i = 0; i < this.fosforoEstacion.length; i++) {
                if (i != 0) {
                    archivo.printf(" ");
                }
                archivo.printf(String.valueOf(this.fosforoEstacion[i]));
            }
            archivo.println("");
            archivo.print("Siguientes Usos = ");
            for (int i = 0; i < this.siguientesUsos.size(); i++) {
                if (i != 0) {
                    archivo.printf(" ");
                }
                archivo.printf(String.valueOf(this.siguientesUsos.get(i)));
            }
            archivo.println("");


            //Agrego el valor de fitness en una nueva linea
            //archivo.append(String.valueOf(this.evaluarFitness())+"\n");
            //archivo.println(String.valueOf(this.evaluarFitness()));
            archivo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //Cargo todos los datos
    }
    /**Martin**/
    public static String getEstacionUso(int numero) {
        if (numero >= 0) {
            return "(" + ((numero / 4) + 1) + "° año)";
        } else {
            return "No match";
        }
    }

    //TODO: parametrizarlo, quizas con while, o haciendo hash map.
    // Este hash map deberia ser unico, y deberia estar en un controladorUso o algo asi que sea singleton, y esta funcion estar ahi (si se usara).
    /**Martin**/
    public static int indiceUso(String nombre) {
        switch (nombre) {
            case "Alfalfa":
                return 1;
            case "FE+TB+L":
                return 2;
            case "TR+Cebadilla":
                return 3;
            case "TR+TB+Raigras":
            case "TR+TB+Raigrás":
            case "TR+TB+RaigrÃ¡s":
                return 4;
            case "Lotus Puro":
                return 5;
            case "Raigras+TB+L":
            case "Raigrás+TB+L":
            case "RaigrÃ¡s+TB+L":
                return 6;
            case "Achicoria":
                return 7;
            case "Moha":
                return 8;
            case "Sorgo Forrajero":
                return 9;
            case "Avena Pastoreo":
                return 10;
            case "Avena+Raigras Temp":
            case "Avena+Raigrás Temp":
            case "Avena+RaigrÃ¡s Temp ":

                return 11;
            case "Maiz":
                return 12;
            case "Campo Natural":
                return 13;
            case "Rastrojo":
                return 14;
            default:

                System.out.println("No match in: indiceUso "+nombre);
            return 0;
        }
    }

    //TODO: Reescribirlo segun duracion del uso, pero que chequee consistencia con los usos validos.
    //TODO: Exepcion No exite el uso.
    //TODO: Exepcion Excede la duracion.
    /**Devuelve segun el nombre del uso  su numero y su duracion**/
    public static int[] usoYDuracion(String usoOriginal) {
        //Devuelve una tupla con el numero de uso en el primer elemento y
        int[] usoYDuracion = new int[2];

        String[] campos = usoOriginal.split("\\(");
        /*for (int iCampos = 0; iCampos < campos.length; iCampos++) {
            System.out.print("UsoOriginal["+iCampos+"]: "+campos[iCampos]);
        }
        System.out.print("\n");
        */
        switch (campos[0]) {

            case "Alfalfa ":
                usoYDuracion[0] = 1;
                break;
            case "FE+TB+L ":
                usoYDuracion[0] = 2;
                break;
            case "TR+Cebadilla ":
                usoYDuracion[0] = 3;
                break;
            case "TR+TB+Raigras ":
            case "TR+TB+Raigrás ":
            case "TR+TB+RaigrÃ¡s":
                usoYDuracion[0] = 4;
                break;
            case "Lotus Puro ":
                usoYDuracion[0] = 5;
                break;
            case "Raigras+TB+L ":
            case "Raigrás+TB+L ":
            case "RaigrÃ¡s+TB+L":
                usoYDuracion[0] = 6;
                break;
            case "Achicoria ":
                usoYDuracion[0] = 7;
                break;
            case "Moha ":
                usoYDuracion[0] = 8;
                break;
            case "Sorgo Forrajero ":
                usoYDuracion[0] = 9;
                break;
            case "Avena Pastoreo ":
                usoYDuracion[0] = 10;
                break;
            case "Avena+Raigras Temp ":
            case "Avena+Raigrás Temp ":
            case "Avena+RaigrÃ¡s Temp ":
                usoYDuracion[0] = 11;
                break;
            case "Maiz ":
                usoYDuracion[0] = 12;
                break;
            case "Campo Natural":
                usoYDuracion[0] = 13;
                break;
            case "Rastrojo ":
                usoYDuracion[0] = 14;
                break;
            default:
                System.out.println("No match in: usoYDuracion "+campos[0]);
                break;
        }
        if ((usoYDuracion[0] == 1) || (usoYDuracion[0] == 2)) {
            switch (campos[1]) {
                case "1° año)":
                case "1Â° aÃ±o)":
                    usoYDuracion[1] = 12;
                    break;
                case "2° año)":
                case "2°año)":
                case "2Â° aÃ±o)":
                case "2Â°aÃ±o)":
                    usoYDuracion[1] = 8;
                    break;
                case "3° año)":
                case "3Â° aÃ±o)":
                    usoYDuracion[1] = 4;
                    break;
                case "4° año)":
                case "4Â° aÃ±o)":
                    usoYDuracion[1] = 0;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + usoYDuracion[0] + " y duracion: " + usoYDuracion[1]);
                    break;
            }
        } else if ((usoYDuracion[0] == 3) || (usoYDuracion[0] == 4)) {
            switch (campos[1]) {
                case "1° año)":
                case "1Â° aÃ±o)":
                    usoYDuracion[1] = 4;
                    break;
                case "2° año)":
                case "2°año)":
                case "2Â° aÃ±o)":
                case "2Â°aÃ±o)":
                    usoYDuracion[1] = 0;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + usoYDuracion[0] + " y duracion: " + usoYDuracion[1]);
                    System.out.println("Con el texto: " + usoOriginal);
                    System.out.println("Parseado: " + campos[0] + " || " + campos[1]);


                    break;
            }
        } else if ((usoYDuracion[0] == 5) || (usoYDuracion[0] == 6)) {
            switch (campos[1]) {
                case "1° año)":
                case "1Â° aÃ±o)":
                    usoYDuracion[1] = 8;
                    break;
                case "2° año)":
                case "2°año)":
                case "2Â° aÃ±o)":
                case "2Â°aÃ±o)":
                    usoYDuracion[1] = 4;
                    break;
                case "3° año)":
                case "3Â° aÃ±o)":
                    usoYDuracion[1] = 0;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + usoYDuracion[0] + " y duracion: " + usoYDuracion[1]);
                    break;
            }
        } else if (usoYDuracion[0] == 7) {
            switch (campos[1]) {
                case "1° año)":
                case "1Â° aÃ±o)":
                    usoYDuracion[1] = 7;
                    break;
                case "2° año)":
                case "2°año)":
                case "2Â° aÃ±o)":
                case "2Â°aÃ±o)":
                    usoYDuracion[1] = 0;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + usoYDuracion[0] + " y duracion: " + usoYDuracion[1]);
                    break;
            }
        } else if (usoYDuracion[0] == 8) {
            switch (campos[1]) {
                case "anual)":
                    usoYDuracion[1] = 0;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + usoYDuracion[0] + " y duracion: " + usoYDuracion[1]);
                    break;
            }
        } else if (usoYDuracion[0] == 9) {
            switch (campos[1]) {
                case "anual)":
                    usoYDuracion[1] = 1;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + usoYDuracion[0] + " y duracion: " + usoYDuracion[1]);
                    break;
            }
        } else if ((usoYDuracion[0] > 10)) {
            usoYDuracion[1] = 0;
            /*switch (campos[1]){
                case "anual)":
                    usoyDuracion[1]=0;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: "+usoYDuracion[0]+" y duracion: " +usoYDuracion[1]");
                    break;
            }*/
        }
        //System.out.println("\tPara el usoOriginal:"+usoOriginal+" Devuelvo uso: "+usoyDuracion[0]+" y duracion: "+usoyDuracion[1]);
        return usoYDuracion;
    }

    //TODO: Reescribirlo segun duracion del uso, pero que chequee consistencia con los usos validos.
    /**Devuelve segun el nombre un numero de uso y estacion 100*Numero de uso + numeroEstacion**/
    public static int obtenerUsoBase(String usoOriginal) {
        int usoBase = 0;

        String[] campos = usoOriginal.split("\\(");
        /*for (int iCampos = 0; iCampos < campos.length; iCampos++) {
            System.out.print("UsoOriginal["+iCampos+"]: "+campos[iCampos]);
        }
        System.out.print("\n");
        */
        switch (campos[0]) {

            case "Alfalfa ":
                usoBase = 100;
                break;
            case "FE+TB+L ":
                usoBase = 200;
                break;
            case "TR+Cebadilla ":
                usoBase = 300;
                break;
            case "TR+TB+Raigras ":
            case "TR+TB+Raigrás ":
            case "TR+TB+RaigrÃ¡s":
                usoBase = 400;
                break;
            case "Lotus Puro ":
                usoBase = 500;
                break;
            case "Raigras+TB+L ":
            case "Raigrás+TB+L ":
            case "RaigrÃ¡s+TB+L":
                usoBase = 600;
                break;
            case "Achicoria ":
                usoBase = 700;
                break;
            case "Moha ":
                usoBase = 800;
                break;
            case "Sorgo Forrajero ":
                usoBase = 900;
                break;
            case "Avena Pastoreo ":
                usoBase = 1000;
                break;
            case "Avena+Raigras Temp ":
            case "Avena+Raigrás Temp ":
            case "Avena+RaigrÃ¡s Temp ":
                usoBase = 1100;
                break;
            case "Maiz ":
                usoBase = 1200;
                break;
            case "Campo Natural":
                usoBase = 1300;
                break;
            case "Rastrojo ":
                usoBase = 1400;
                break;
            default:
                System.out.println("No match in: obtenerUsoBase "+campos[0]);
                break;
        }
        if ((usoBase == 100) || (usoBase == 200)) {
            switch (campos[1]) {
                case "1° año)":
                case "1Â° aÃ±o)":
                    usoBase += 4;
                    break;
                case "2° año)":
                case "2°año)":
                case "2Â° aÃ±o)":
                case "2Â°aÃ±o)":
                    usoBase += 8;
                    break;
                case "3° año)":
                case "3Â° aÃ±o)":
                    usoBase += 12;
                    break;
                case "4° año)":
                case "4Â° aÃ±o)":
                    usoBase += 16;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + campos[0] + " y duracion: " + campos[1]);
                    System.out.println("Con el texto: " + usoOriginal);
                    System.out.println("Parseado: " + campos[0] + " || " + campos[1]);
                    break;
            }
        } else if ((usoBase == 300) || (usoBase == 400)) {
            switch (campos[1]) {
                case "1° año)":
                case "1Â° aÃ±o)":
                    usoBase += 4;
                    break;
                case "2° año)":
                case "2°año)":
                case "2Â° aÃ±o)":
                case "2Â°aÃ±o)":
                    usoBase += 8;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + campos[0] + " y duracion: " + campos[1]);
                    System.out.println("Con el texto: " + usoOriginal);
                    System.out.println("Parseado: " + campos[0] + " || " + campos[1]);


                    break;
            }
        } else if ((usoBase == 500) || (usoBase == 600)) {
            switch (campos[1]) {
                case "1° año)":
                case "1Â° aÃ±o)":
                    usoBase += 4;
                    break;
                case "2° año)":
                case "2°año)":
                case "2Â° aÃ±o)":
                case "2Â°aÃ±o)":
                    usoBase += 8;
                    break;
                case "3° año)":
                case "3Â° aÃ±o)":
                    usoBase += 12;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + campos[0] + " y duracion: " + campos[1]);
                    System.out.println("Con el texto: " + usoOriginal);
                    System.out.println("Parseado: " + campos[0] + " || " + campos[1]);
                    break;
            }
        } else if (usoBase == 700) {
            switch (campos[1]) {
                case "1° año)":
                case "1Â° aÃ±o)":
                    usoBase += 4;
                    break;
                case "2° año)":
                case "2°año)":
                case "2Â° aÃ±o)":
                case "2Â°aÃ±o)":
                    usoBase += 7;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + campos[0] + " y duracion: " + campos[1]);
                    System.out.println("Con el texto: " + usoOriginal);
                    System.out.println("Parseado: " + campos[0] + " || " + campos[1]);
                    break;
            }
        } else if (usoBase == 800) {
            switch (campos[1]) {
                case "anual)":
                    usoBase += 2;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + campos[0] + " y duracion: " + campos[1]);
                    System.out.println("Con el texto: " + usoOriginal);
                    System.out.println("Parseado: " + campos[0] + " || " + campos[1]);
                    break;
            }
        } else if (usoBase == 900) {
            switch (campos[1]) {
                case "anual)":
                    usoBase += 3;
                    break;
                default:
                    System.out.println("No corresponde la duracion. Para Uso: " + campos[0] + " y duracion: " + campos[1]);
                    System.out.println("Con el texto: " + usoOriginal);
                    System.out.println("Parseado: " + campos[0] + " || " + campos[1]);
                    break;
            }
        } else if (usoBase == 1000) {
            usoBase += 3;
        } else if (usoBase == 1100) {
            usoBase += 3;
        } else if (usoBase == 1200) {
            usoBase += 2;
        } else if (usoBase == 1300) {
            usoBase += 4;
        } else if (usoBase == 1400) {
            usoBase += 1;
        } else {
            System.out.println("No corresponde la duracion. Para Uso: " + campos[0] + " y duracion: " + campos[1]);
            System.out.println("Con el texto: " + usoOriginal);
            System.out.println("Parseado: " + campos[0] + " || " + campos[1]);
        }

        //System.out.println("\tPara el usoOriginal:"+usoOriginal+" Devuelvo uso: "+usoyDuracion[0]+" y duracion: "+usoyDuracion[1]);
        return usoBase;
    }
    /**Genera un uso reservado, para ser usado en caso de querer sortear entre todos los usos posibles (salvo Campo Natural).**/
    public static Uso obtenerUsoReservado() {
        //Genera un uso reservado, para ser usado en caso de querer sortear entre todos los usos posibles (salvo Campo Natural).
        float[] productividadUso, fosforoEstacion;
        List<Integer> siguientesUsos;
        //Siguientes usos 0 reservado para el inicio random
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14));
        fosforoEstacion = new float[]{0};
        productividadUso = new float[]{0};
        return new Uso(0, 0, 0, 0, productividadUso, fosforoEstacion, "Reservado", siguientesUsos);
    }
    /**Carga los usos bases HARDCODEADOS**/
    public static Uso[] cargarUsos() {
        Uso[] listaUsos = new Uso[Constantes.cantUsos];
        float[] productividadUso, fosforoEstacion;
        List<Integer> siguientesUsos;
        //Siguientes usos 0 reservado para el inicio random
        siguientesUsos = new ArrayList<Integer>(Arrays.asList());
        fosforoEstacion = new float[]{0};
        productividadUso = new float[]{0};
        listaUsos[0] = new Uso(0, 0, 0, 0, productividadUso, fosforoEstacion, "Reservado", siguientesUsos);

        //Cargo los usos reales

        //Pastura Perenne 1 ---- Alfalfa
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 14));
        fosforoEstacion = new float[]{0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f, 0.513f};
        productividadUso = new float[]{0, 0, 3025, 2475, 3000, 1200, 4200, 3600, 1600, 800, 3200, 2400, 700, 350, 3500, 2450};

        listaUsos[1] = new Uso(1, 16, 0, 4.64f, productividadUso, fosforoEstacion, "Alfalfa", siguientesUsos);

        //Pastura Perenne 2 ---- FE+TB+L
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 14));
        fosforoEstacion = new float[]{0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f};
        productividadUso = new float[]{0, 450, 3150, 900, 2500, 1500, 4000, 2000, 1400, 1120, 3500, 980, 700, 650, 3000, 650};
        listaUsos[2] = new Uso(2, 16, 0, 4.64f, productividadUso, fosforoEstacion, "FE+TB+L", siguientesUsos);

        //Pastura Perenne 3 ---- TR+Cebadilla
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 14));
        productividadUso = new float[]{0, 2000, 4400, 1600, 2800, 2000, 4200, 1000};
        fosforoEstacion = new float[]{0.43f, 0.43f, 0.43f, 0.43f, 0.43f, 0.43f, 0.43f, 0.43f};
        listaUsos[3] = new Uso(3, 8, 0, 2.32f, productividadUso, fosforoEstacion, "TR+Cebadilla", siguientesUsos);

        //Pastura Perenne 4 ---- TR+TB+Raigras
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 14));
        productividadUso = new float[]{1161, 2212, 3768, 1483, 2176, 1780, 3495, 1483};
        fosforoEstacion = new float[]{0.43f, 0.43f, 0.43f, 0.43f, 0.43f, 0.43f, 0.43f, 0.43f};
        listaUsos[4] = new Uso(4, 8, 0, 2.32f, productividadUso, fosforoEstacion, "TR+TB+Raigrás", siguientesUsos);

        //Pastura Perenne 5 ---- Lotus Puro
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 14));
        productividadUso = new float[]{558, 1075, 2127, 962, 1318, 1107, 2826, 1387, 1134, 828, 2340, 971};
        fosforoEstacion = new float[]{0.473f, 0.473f, 0.473f, 0.473f, 0.473f, 0.473f, 0.473f, 0.473f, 0.473f, 0.473f, 0.473f, 0.473f};
        listaUsos[5] = new Uso(5, 12, 0, 3.48f, productividadUso, fosforoEstacion, "Lotus Puro", siguientesUsos);

        //Pastura Perenne 6 ---- Raigras+TB+L
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 14));
        productividadUso = new float[]{775, 1811, 3159, 816, 1840, 1749, 3342, 1365, 1318, 1287, 2523, 999};
        fosforoEstacion = new float[]{0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f, 0.345f};
        listaUsos[6] = new Uso(6, 12, 0, 3.48f, productividadUso, fosforoEstacion, "Raigrás+TB+L", siguientesUsos);

        //Pastura Perenne 7 ---- Achicoria
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 14));
        productividadUso = new float[]{65, 232, 2653, 2849, 1572, 2342, 3193};
        fosforoEstacion = new float[]{0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f};
        listaUsos[7] = new Uso(7, 7, 0, 2.03f, productividadUso, fosforoEstacion, "Achicoria", siguientesUsos);

        //Verdeo Verano 8 ---- Moha
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 10, 11, 14));
        productividadUso = new float[]{0, 5000};
        fosforoEstacion = new float[]{0.393f, 0.393f};
        listaUsos[8] = new Uso(8, 2, 1, 1.16f, productividadUso, fosforoEstacion, "Moha", siguientesUsos);

        //Verdeo Verano 9 ---- Sorgo Forrajero
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 5, 7, 8, 9, 12, 14));
        productividadUso = new float[]{0, 9687, 3568};
        fosforoEstacion = new float[]{0.548f, 0.548f, 0.548f};
        listaUsos[9] = new Uso(9, 3, 1, 1.16f, productividadUso, fosforoEstacion, "Sorgo Forrajero", siguientesUsos);

        //Verdeo Verano 10 ---- Avena Pastoreo
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 5, 7, 8, 9, 12, 14));
        productividadUso = new float[]{1625, 3250, 1625};
        fosforoEstacion = new float[]{0.520f, 0.520f, 0.520f};
        listaUsos[10] = new Uso(10, 3, 0, 1.16f, productividadUso, fosforoEstacion, "Avena Pastoreo", siguientesUsos);

        //Verdeo Verano 11 ---- Avena+Raigras Temp.
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(8, 9, 12, 14));
        productividadUso = new float[]{2083, 2211, 2735};
        fosforoEstacion = new float[]{0.520f, 0.520f, 0.520f};
        listaUsos[11] = new Uso(11, 3, 0, 1.16f, productividadUso, fosforoEstacion, "Avena+Raigrás Temp", siguientesUsos);

        //Cultivo Verano 12 ---- Maiz
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 10, 11, 14));
        productividadUso = new float[]{0, 12882};
        fosforoEstacion = new float[]{0.560f, 0.560f};
        listaUsos[12] = new Uso(12, 2, 1, 2.230f, productividadUso, fosforoEstacion, "Maiz", siguientesUsos);

        //Campo Natural 13 ---- Campo Natural
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
        productividadUso = new float[]{675, 425, 1039, 932};
        fosforoEstacion = new float[]{0.168f, 0.168f, 0.168f, 0.168f};
        listaUsos[13] = new Uso(13, 4, 2, 0.24f, productividadUso, fosforoEstacion, "Campo Natural", siguientesUsos);

        //Rastrojo 14 ---- Rastrojo
        //Usa el mismo uso que el anterior
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14));
        productividadUso = new float[]{0, 0, 0, 0};//
        fosforoEstacion = new float[]{0.244f, 0.244f, 0.244f, 0.244f};
        listaUsos[14] = new Uso(14, 1, 2, 0.975f, productividadUso, fosforoEstacion, "Rastrojo", siguientesUsos);

        return listaUsos;
    }
    /**Carga usos test**/
    public static Uso[] cargarUsosTest() {
        /*
        cantPixelesTest = 10;
        cantEstacionesTest = 8;
        cantAniosTest = cantEstacionesTest/4;
        cantUsosTest= 5;
        cantProductoresTest= 2;
        topeFosforoAnualTest= new float[cantAniosTest];*/

        Uso[] listaUsos = new Uso[Constantes.cantUsos]; //4 en el test 1
        float[] productividadUso, fosforoEstacion;
        List<Integer> siguientesUsos;

        //Siguientes usos 0 reservado para el inicio random
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4));
        productividadUso = new float[]{0};
        fosforoEstacion = new float[]{0f};
        listaUsos[0] = new Uso(0, 0, 0, 0, productividadUso, fosforoEstacion, "Cero", siguientesUsos);

        //Cargo los usos del test

        //Siguientes usos  y productividad para las UsoTest1
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(3));
        productividadUso = new float[]{10};
        fosforoEstacion = new float[]{10f};
        listaUsos[1] = new Uso(1, 1, 2, 10f, productividadUso, fosforoEstacion, "Uno", siguientesUsos);

        //Siguientes usos  y productividad para las UsoTest2
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(3, 4));
        productividadUso = new float[]{20, 20};
        fosforoEstacion = new float[]{10f, 10f};
        listaUsos[2] = new Uso(2, 2, 2, 20, productividadUso, fosforoEstacion, "Dos", siguientesUsos);

        //Siguientes usos  y productividad para las UsoTest3
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(1));
        productividadUso = new float[]{30, 30, 30};
        fosforoEstacion = new float[]{10f, 10f, 10f};
        listaUsos[3] = new Uso(3, 3, 2, 30, productividadUso, fosforoEstacion, "Tres", siguientesUsos);

        //Siguientes usos  y productividad para las UsoTest4
        siguientesUsos = new ArrayList<Integer>(Arrays.asList(4));
        productividadUso = new float[]{40, 40, 40, 40};
        fosforoEstacion = new float[]{10f, 10f, 10f, 10f};
        listaUsos[4] = new Uso(4, 4, 2, 40, productividadUso, fosforoEstacion, "Cuatro", siguientesUsos);
        return listaUsos;
    }
    /**Chequea si un uso tiene usos siguientes**/
    public boolean tengoSiguiente(ArrayList<Integer> usosDelProductorEstaEstacion) {
        for (Integer siguienteUso : this.siguientesUsos) {
            if (usosDelProductorEstaEstacion.contains(siguienteUso)) {
                return true;
            }
        }
        return false;
    }
    /**Imprime los siguientes usos de este uso**/
    public void imprimirSiguientesUsos() {
        System.out.print("Uso[" + this.numUso + "] Siguientes:");
        for (int iUso = 0; iUso < this.siguientesUsos.size(); iUso++) {
            System.out.print(" " + this.siguientesUsos.get(iUso));
        }
        System.out.print("\n");
    }
}
