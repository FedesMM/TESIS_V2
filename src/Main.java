import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;



public class Main {
    /**Ejecuta generarMejor solucion**/
    public static void main(final String[] args) {
        //boolean imprimirConstantesTodoJunto = false, imprimirConstantesUnoAUno = false, imprimirSolucion = false;
        //Solucion solucion, solucionOriginal;
        //int cantidadSoluciones = 0, cantidadFI = 0;
        Constantes.usos = Uso.cargarUsos();
        //Uso.imprimirUsos();
        //Se cargan primero los productores para que al agregar los pixeles se incerten cuales pertenecen a cada productor
        Constantes.productores = Productor.cargarProductores();
        //Main.crearInstanciasProductores();


        //Productor.imprimirProductores();
        //String fileName = "potreros.in";
        //Constantes.cantPixeles = 792;
        //Constantes.cantPotreros = 1602;
        //String fileName = "pixeles.in";
        //Constantes.cantPixeles = 18475;
        //Constantes.cantPotreros = 26168;
        //Constantes.pixeles = Pixel.cargarPixeles(fileName);


        //GENERADOR DE .DAT



        //Main.resolverProductores();
        Main.generarMejorSolucion();
        //Main.testCargarUsos();
        //Main.testInstancias();
        //Main.testFactibilizarProductividad();
        //Main.testCrearSolucionFactible();
       // Main.testGRASPAlgEvo();

    }
    /**Carrga y resuelve el problema para un procutor ejecutando tantas repeticiones como se le pida,
     * leyendo sus datos desde Instancias/productores/productor<Numero>.in
     * **/
    private static void resolverProductor() {
        int cantidadSoluciones = 0, datos;
        Solucion solucion;
        Scanner reader = new Scanner(System.in);


        System.out.print("Cantidad de soluciones a generar : ");
        cantidadSoluciones = reader.nextInt();

        System.out.print("Numero de productor: ");
        datos = reader.nextInt();
        Constantes.pixeles=Pixel.cargarPixelesDeProductor("Instancias/productores/productor"+datos+".in",datos);

        solucion=Main.grasp(cantidadSoluciones, true, false);

        Main.crearArchivos(solucion, "FuncionObjetivo");
        System.out.println("Mejor solucion:");
        System.out.println("\tFosforo: " + solucion.fosforo);
        solucion.imprimirRestriccionProductividadMinimaEstacion();
        solucion.imprimirRestriccionUsosDistintos();
        reader.close();
    }
    /**Resuelve el problema para cada uno de los productores por separado, leyendo sus datos desde
     * Instancias/productores/productor<Numero>.in  y guardadno el resultado en soluciones/productor<Numero>.out**/
    private static void resolverProductores() {
        Solucion solucion;

        for (int iProductores = 13; iProductores <=42 ; iProductores++) {
            Constantes.pixeles=Pixel.cargarPixelesDeProductor("Instancias/productores/productor"+iProductores+".in",iProductores);

            solucion=Main.grasp(100000, true, false);

            solucion.crearArchivoPlanificacion("soluciones/productor"+iProductores+".out");
            System.out.println("Mejor solucion:");
            System.out.println("\tFosforo: " + solucion.fosforo);
            solucion.imprimirRestriccionProductividadMinimaEstacion();
            solucion.imprimirRestriccionUsosDistintos();

        }


    }
    /**Genera archvio de datos para cada productor**/
    private static void crearInstanciasProductores(){
        for (int iProductor = 1; iProductor <= Constantes.cantProductores; iProductor++) {
            Constantes.cargarInstanciaProductor(iProductor);
            Main.generarArchvioDeDatos(iProductor);
        }
    }
    /**Carga usos, los imprime, los limpia, los vuelve a cargar, los vuelve a imprimier y ejecuta generarMejorSolucion**/
    private static void testCargarUsos() {

        String fileName = "potreros.in";
        Constantes.cantPixeles = 792;
        Constantes.pixeles = Pixel.cargarPixeles(fileName);
        System.out.println("TEST CAGAR USOS");
        Uso.imprimirUsos();
        Uso.crearArchivosUsos();
        System.out.println("Limpio los usos");
        Constantes.usos= null;
        System.out.println("Recargo los usos");
        Scanner reader = new Scanner(System.in);
        reader.next();
        Uso.cargarUsosDesdeArchivos();
        System.out.println();
        Uso.imprimirUsos();
        Main.generarMejorSolucion();
        reader.close();

    }
    /**Calcula para 100 repeticiones y con una profundidad de 100 calcula cuenta cuantas veces se alcanza la factibilidad
     * al crear una solicion, crear uan factible, factibilizar segun usos y factibilizar segun produccion**/
    private static void testCrearSolucionFactible() {
        Solucion solucion, solucionCantUsos, solucionProductividad, solucionFactible;
        String nombreInstancia;
        int cantPixeles, repeticiones=100, profundidad=100;
        int factible=0, cumpleRCantUsos=0, cumpleRProductividad=0;
        int factible2=0, cumpleRCantUsos2=0, cumpleRProductividad2=0;
        int factible3=0, cumpleRCantUsos3=0, cumpleRProductividad3=0;
        int factible4=0, cumpleRCantUsos4=0, cumpleRProductividad4=0;


        int instancia=29;
        System.out.println("testFactibilizarCantUsos");
        for (int iInstancia =0; iInstancia <= instancia; iInstancia++) {
            int maxSoluciones=1000;
            factible=0; cumpleRCantUsos=0; cumpleRProductividad=0;
            factible2=0; cumpleRCantUsos2=0; cumpleRProductividad2=0;
            factible3=0; cumpleRCantUsos3=0; cumpleRProductividad3=0;
            factible4=0; cumpleRCantUsos4=0; cumpleRProductividad4=0;

            nombreInstancia = "./Instancias/Intancia " + (iInstancia + 1) + ".in";
            cantPixeles=Pixel.contarLineas(nombreInstancia);
            System.out.println(nombreInstancia+"\tCantPixeles :"+cantPixeles+ "\t CantSoluciones: "+maxSoluciones);
            //System.out.print("\tSolucion:                    ");
            for (int iSoluciones = 0; iSoluciones< maxSoluciones; iSoluciones++) {
                System.out.print("\rSolucion: "+iSoluciones);
                Constantes.productores = Productor.cargarProductores();
                Constantes.cantPixeles=cantPixeles;
                Constantes.cantPotreros=cantPixeles;
                Constantes.pixeles = Pixel.cargarPixeles(nombreInstancia);
                Constantes.maximoIncumplimientoUsosDistintos=Constantes.cantEstaciones*Constantes.productoresActivos.size();

                //System.out.println("Solucion: "+iSoluciones);
                solucion=Solucion.crearSolucionFactible();
                solucion.recalcular();
                if(solucion.esFactible()) { factible++; }
                if(solucion.restriccionUsosDistintos.cumpleRestriccion) { cumpleRCantUsos++; }
                if(solucion.restriccionProductividadMinimaEstacion.cumpleRestriccion) { cumpleRProductividad++; }

                solucionCantUsos=solucion.clone();
                solucionCantUsos.factibilizarCantUsos();
                solucionCantUsos.recalcular();
                if(solucionCantUsos.esFactible()) { factible2++; }
                if(solucionCantUsos.restriccionUsosDistintos.cumpleRestriccion) { cumpleRCantUsos2++; }
                if(solucionCantUsos.restriccionProductividadMinimaEstacion.cumpleRestriccion) { cumpleRProductividad2++; }

                solucionProductividad=solucion.clone();
                solucionProductividad.factibilizarProductividad();
                solucionProductividad.recalcular();
                if(solucionProductividad.esFactible()) { factible3++; }
                if(solucionProductividad.restriccionUsosDistintos.cumpleRestriccion) { cumpleRCantUsos3++; }
                if(solucionProductividad.restriccionProductividadMinimaEstacion.cumpleRestriccion) { cumpleRProductividad3++; }

                solucionFactible=solucion.clone();
                solucionFactible.recalcular();
                solucionFactible.factibilizar(repeticiones, profundidad);
                solucionFactible.recalcular();
                if(solucionFactible.esFactible()) { factible4++; }
                if(solucionFactible.restriccionUsosDistintos.cumpleRestriccion) { cumpleRCantUsos4++; }
                if(solucionFactible.restriccionProductividadMinimaEstacion.cumpleRestriccion) { cumpleRProductividad4++; }

            }
            System.out.print("\n\tGENERADA: ");
            System.out.print("\t\t\tFACTIBLE: "+factible);
            System.out.print("\tRest Cant Usos: "+cumpleRCantUsos);
            System.out.println("\t\tRest Productividad: "+cumpleRProductividad);
            System.out.print("\tFacCantUsos: ");
            System.out.print("\t\tFACTIBLE: "+factible2);
            System.out.print("\tRest Cant Usos: "+cumpleRCantUsos2);
            System.out.println("\t\tRest Productividad: "+cumpleRProductividad2);
            System.out.print("\tFacProduccion: ");
            System.out.print("\t\tFACTIBLE: "+factible3);
            System.out.print("\tRest Cant Usos: "+cumpleRCantUsos3);
            System.out.println("\t\tRest Productividad: "+cumpleRProductividad3);
            System.out.print("\tFactibilizar: ");
            System.out.print("\t\tFACTIBLE: "+factible4);
            System.out.print("\tRest Cant Usos: "+cumpleRCantUsos4);
            System.out.println("\t\tRest Productividad: "+cumpleRProductividad4);


            //solucion.imprimirMatriz();
            System.out.println("\n---------------------------------------------------------------------------------\n");
        }
    }
    /**Para 1000 repeticiones  se genera una solucion e intenta factibilizar  por cantidad de usos
     * hasta 1000 veces, imprimiendo si no se logra o cuantas iteraciones demoro en lograrse**/
    private static void testFactibilizarCantUsos() {
        Solucion solucion, nuevaSolucion;
        String nombreInstancia;
        int cantPixeles;
        int instancia=0;
        System.out.println("testFactibilizarCantUsos");
        for (int iInstancia =instancia; iInstancia <= instancia; iInstancia++) {
           int maxSoluciones=1000;
            for (int iSoluciones = 0; iSoluciones< maxSoluciones; iSoluciones++) {
                Constantes.productores = Productor.cargarProductores();
                nombreInstancia = "./Instancias/Intancia " + (iInstancia + 1) + ".in";
                cantPixeles=Pixel.contarLineas(nombreInstancia);
                System.out.println(nombreInstancia+"\tCantPixeles:"+cantPixeles);
                Constantes.cantPixeles=cantPixeles;
                Constantes.cantPotreros=cantPixeles;
                Constantes.pixeles = Pixel.cargarPixeles(nombreInstancia);
                Constantes.maximoIncumplimientoUsosDistintos=Constantes.cantEstaciones*Constantes.productoresActivos.size();

                System.out.println("Solucion: "+iSoluciones);
                solucion=Solucion.crearSolucion();
                for (;solucion.restriccionUsosDistintos.cumpleRestriccion;) {
                    solucion=Solucion.crearSolucion();
                    solucion.recalcular();
                    solucion.imprimirFuncionObjetivo();

                }
                nuevaSolucion= solucion.clone();
                int maxIter=1000;
                for (int i = 0; (i <maxIter&& !nuevaSolucion.restriccionUsosDistintos.cumpleRestriccion); i++) {
                    nuevaSolucion= solucion.clone();
                    System.out.println("Factibilizacion  :"+i);
                    nuevaSolucion.factibilizarCantUsos();
                    nuevaSolucion.recalcular();
                    if (nuevaSolucion.restriccionUsosDistintos.cumpleRestriccion){
                        System.out.println("FACTIBILIZADO EN LA ITERACION  "+i);
                        nuevaSolucion.imprimirUsosDisitintosPorEstacion();
                        nuevaSolucion.evaluarFuncionObjetivo();
                    }else if (i==maxIter-1) {
                        System.out.println("NO FACTIBILZADO "+i);
                        nuevaSolucion.imprimirUsosDisitintosPorEstacion();
                    }else{
//                    System.out.println("NO FACTIBILZADO "+i);
//                    nuevaSolucion.imprimirUsosDisitintosPorEstacion();
                    }
                }
                System.out.println("\n");
            }

            //solucion.imprimirMatriz();
            System.out.println("\n\n");
        }

    }
    /**Para 1000 repeticiones  se genera una solucion e intenta factibilizar  por productividad
     * hasta 1000 veces, imprimiendo si no se logra o cuantas iteraciones demoro en lograrse**/
    private static void testFactibilizarProductividad() {
        Solucion solucion, nuevaSolucion;
        String nombreInstancia;
        int cantPixeles;
        int instancia=14;
        System.out.println("testFactibilizarProductividad");
        for (int iInstancia =instancia; iInstancia <= instancia; iInstancia++) {
            int maxSoluciones=1000;
            nombreInstancia = "./Instancias/Intancia " + (iInstancia + 1) + ".in";
            cantPixeles=Pixel.contarLineas(nombreInstancia);
            System.out.println(nombreInstancia+"\tCantPixeles:"+cantPixeles);
            Constantes.cantPixeles=cantPixeles;
            Constantes.cantPotreros=cantPixeles;
            for (int iSoluciones = 0; iSoluciones< maxSoluciones; iSoluciones++) {
                Constantes.productores = Productor.cargarProductores();
                Constantes.pixeles = Pixel.cargarPixeles(nombreInstancia);
                Constantes.maximoIncumplimientoUsosDistintos=Constantes.cantEstaciones*Constantes.productoresActivos.size();
                //Pixel.imprimirPixeles();
                System.out.println("SOLUCION: "+iSoluciones+"\nORIGINAL:");
                solucion=Solucion.crearSolucion();
                for (;solucion.restriccionProductividadMinimaEstacion.cumpleRestriccion || solucion.restriccionUsosDistintos.cumpleRestriccion;) {
                    solucion=Solucion.crearSolucion();
                    solucion.recalcular();
                }
                solucion.imprimirFuncionObjetivo();

                nuevaSolucion= solucion.clone();
                int maxBusquedas=100, maxMejoras=100;
                for (int i = 0; (i <maxBusquedas && !nuevaSolucion.esFactible()); i++) {
                    nuevaSolucion= solucion.clone();
                    //System.out.println("Factibilizacion  :"+i);
                    for (int j = 0; (j < maxMejoras &&  !nuevaSolucion.esFactible()) ; j++) {
                        nuevaSolucion.factibilizarProductividad();
                        nuevaSolucion.recalcular();
                    }
                    if (nuevaSolucion.esFactible()){
                        System.out.println("FACTIBILIZADO EN LA ITERACION  "+i);
                        //nuevaSolucion.imprimirProductividadSobreSuperficiePorEstacion();
                        nuevaSolucion.imprimirFuncionObjetivo();
                        if(!nuevaSolucion.restriccionUsosDistintos.cumpleRestriccion){
                            nuevaSolucion.imprimirRestriccionUsosDistintosExpandida();
                            nuevaSolucion.imprimirMatriz();
                        }
                    }else if (i==maxBusquedas-1) {
                        System.out.println("NO FACTIBILIZADO "+i);
                        //nuevaSolucion.imprimirProductividadSobreSuperficiePorEstacion();
                        nuevaSolucion.imprimirFuncionObjetivo();
                    }else{
//                    System.out.println("NO FACTIBILIZADO "+i);
//                    nuevaSolucion.imprimirUsosDisitintosPorEstacion();
                    }
                }
                System.out.println("\n");
            }

            //solucion.imprimirMatriz();
            System.out.println("\n-------------------------------------------------------------------------------------------\n");
        }

    }
    /**Testeo cada instancia calculando varias ejecuciones de cada una y deolviendo su mediana y varianza**/
    private static void testInstancias() {
        Solucion solucion;
        String nombreInstancia;
        int cantPixeles=0;
        float fosforosRepeticion[]=new float[30], usosRepeticion[]=new float[30],productividadRepeticion[]=new float[30];
        float fosforoEsperanza[]=new float[30], usosEsperanza[]=new float[30], productividadEsperanza[]=new float[30];
        float cantIncumplimientosProductividadRepeticion[]=new float [30], cantIncumplimientosProductividadEsperanza[]=new float[30];
        float cantIncumplimientosUsosRepeticion[]=new float [30], cantIncumplimientosUsosEsperanza[]=new float[30];;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(6);
        df.setMinimumFractionDigits(6);
        //Para cada instancia
        for (int iInstancia = 0; iInstancia < 30; iInstancia++) {
            nombreInstancia="./Instancias/Intancia "+(iInstancia+1)+".in";
            cantPixeles=Pixel.contarLineas(nombreInstancia);
            Constantes.cantPixeles=cantPixeles;
            Constantes.cantPotreros=cantPixeles;
            Constantes.pixeles = Pixel.cargarPixeles(nombreInstancia);
            Constantes.maximoIncumplimientoUsosDistintos=Constantes.cantEstaciones*Constantes.productoresActivos.size();
            System.out.println(nombreInstancia+"\tCantPixeles:"+cantPixeles);
            //Ejecuto el grasp 30 veces para cada instancia
            for (int iRepeticion = 0; iRepeticion < 10; iRepeticion++) {
                System.out.print("\t\tRepeticion "+iRepeticion);
                //Pixel.imprimirPixeles();
                //Productor.imprimirProductores();
                solucion=Main.grasp(1000, false, false);
                //Cuento las lineas
                //Corro el grasp
                //System.out.println("\tCantPixeles: "+ Constantes.cantPixeles+"\tCantEstaciones: "+Constantes.cantEstaciones);

                //Guardo datos
                solucion.recalcular();
                fosforosRepeticion[iRepeticion]=solucion.evaluarFosforoModulado();
                productividadRepeticion[iRepeticion]=solucion.evaluarIncumplimientoProductividadModulado();
                usosRepeticion[iRepeticion]=solucion.evaluarIncumplimientoUsosDistintosModulado();
                cantIncumplimientosUsosRepeticion[iRepeticion]=solucion.restriccionUsosDistintos.cantIncumplimientos;
                cantIncumplimientosProductividadRepeticion[iRepeticion]=solucion.restriccionProductividadMinimaEstacion.cantIncumplimientos;


                fosforoEsperanza[iInstancia]+=solucion.evaluarFosforoModulado();
                productividadEsperanza[iInstancia]+=solucion.evaluarIncumplimientoProductividadModulado();
                usosEsperanza[iInstancia]+=solucion.evaluarIncumplimientoUsosDistintosModulado();
                cantIncumplimientosUsosRepeticion[iRepeticion]+=solucion.restriccionUsosDistintos.cantIncumplimientos;
                cantIncumplimientosProductividadEsperanza[iInstancia]+=solucion.restriccionProductividadMinimaEstacion.cantIncumplimientos;

                //solucion.imprimirFuncionObjetivo();
                System.out.print("\tFosforo Modulado: "+df.format(fosforosRepeticion[iRepeticion]));
                System.out.print("\tProductividad Modulada: "+ df.format(productividadRepeticion[iRepeticion]));
                System.out.print("\tProductividad CantIncumplimientos: "+cantIncumplimientosProductividadRepeticion[iRepeticion]);
                System.out.print("/"+ Constantes.productoresActivos.size()*Constantes.cantEstaciones);
                System.out.print("\tUsos Distintos Modulado: "+ df.format(usosRepeticion[iRepeticion]));
                System.out.print("\tUsos Distintos CantIncumplimientos: "+cantIncumplimientosUsosRepeticion[iRepeticion]);
                System.out.println("/"+ Constantes.productoresActivos.size()*Constantes.cantEstaciones);
            }
            //Tomo la media
            fosforoEsperanza[iInstancia]=fosforoEsperanza[iInstancia]/30;
            productividadEsperanza[iInstancia]=productividadEsperanza[iInstancia]/30;
            usosEsperanza[iInstancia]=usosEsperanza[iInstancia]/30;
            cantIncumplimientosProductividadEsperanza[iInstancia]=cantIncumplimientosProductividadEsperanza[iInstancia]/30;
            cantIncumplimientosUsosEsperanza[iInstancia]=cantIncumplimientosUsosEsperanza[iInstancia]/30;

            System.out.print("\tMedia de Fosforo Modulado: "+df.format(fosforoEsperanza[iInstancia]));
            System.out.print("\tMedia de Productividad Modulada: "+ df.format(productividadEsperanza[iInstancia]));
            System.out.print("\tMedia Productividad CantIncumplimientos: "+cantIncumplimientosProductividadEsperanza[iInstancia]);
            System.out.print("/"+ Constantes.productoresActivos.size()*Constantes.cantEstaciones);
            System.out.print("\tMedia de Usos Distintos Modulado: "+ df.format(usosEsperanza[iInstancia])+"\n\n");
            System.out.print("\tMedia Usos Distintos CantIncumplimientos: "+cantIncumplimientosUsosEsperanza[iInstancia]);
            System.out.println("/"+ Constantes.productoresActivos.size()*Constantes.cantEstaciones);

        }

        //Muestro datos

    }
    /**Ejecuta una busqueda GRASP con tantas repeticiones como maxCantidad, desplegando informacion segun verbose, y
     * contemplando la distanciaAlRio o no segun se lo especifique**/
    private static Solucion grasp(int maxCantidad, boolean verbose, boolean distanciaAlRio) {
        Solucion nuevaSolucion, mejorSolucion;
        //System.out.println("GRASP-Cantidad maxima de Soluciones:");
        //int maxCantidad = 0;
        //Scanner reader = new Scanner(System.in);
        //maxCantidad = reader.nextInt();

        mejorSolucion = Main.LocalSearch(Solucion.crearSolucion(), distanciaAlRio);
        if (verbose) System.out.println("GRASP-Solucion Original: ");
        mejorSolucion.evaluarFuncionObjetivo();

        for (int iSoluciones = 0; iSoluciones < maxCantidad; iSoluciones++) {
            if (verbose)System.out.println("GRASP-Iteracion: " + iSoluciones);
            nuevaSolucion=Solucion.crearSolucion();
            nuevaSolucion=Main.LocalSearch(nuevaSolucion, distanciaAlRio);

            if (nuevaSolucion.evaluarFuncionObjetivo() < mejorSolucion.evaluarFuncionObjetivo()) {
                if (verbose)System.out.println("GRASP-Actualizo el mejor.");
                if (verbose)System.out.println("GRASP-Mejor Solucion previa: "+ mejorSolucion.evaluarFuncionObjetivo());

                mejorSolucion = nuevaSolucion.clone();
                if (verbose)System.out.println("GRASP-Mejor Solucion actual: "+mejorSolucion.evaluarFuncionObjetivo());
            }else{
                if (verbose)System.out.println("GRASP-Conservo el mejor anterior ");
                if (verbose)System.out.println("GRASP-Mejor Solucion previa: "+ mejorSolucion.evaluarFuncionObjetivo());
            }
            if (verbose)System.out.println();
            if (verbose)System.out.println();
        }

        if (verbose)System.out.println("GRASP-Solucion Final: ");
        return mejorSolucion;

    }
    /**Ejecuta una busqueda GRASP con tantas repeticiones como maxCantidad, la que se corta si en tantas repeticiones
     * como condicionParada no hubo mejora, desplegando informacion segun verbose y contemplando la distanciaAlRio
     * si se lo especifica.**/
    private static Solucion graspALgEvo(int maxCantidad, int condicionParada, boolean verbose, boolean distanciaAlRio) {
        Solucion nuevaSolucion, mejorSolucion;
        int ultimaIteracion=0;
        long ultimaMejora=System.currentTimeMillis();
        long mejoraActual=ultimaMejora;
        //System.out.println("GRASP-Cantidad maxima de Soluciones:");
        //int maxCantidad = 0;
        //Scanner reader = new Scanner(System.in);
        //maxCantidad = reader.nextInt();

        mejorSolucion = Main.LocalSearch(Solucion.crearSolucion(), distanciaAlRio);
        if (verbose) System.out.println("GRASP-Solucion Original: ");
        mejorSolucion.evaluarFuncionObjetivo();

        for (int iSoluciones = 0; (iSoluciones < maxCantidad && (iSoluciones< (ultimaIteracion+condicionParada))); iSoluciones++) {
            if (verbose)System.out.println("GRASP-Iteracion: " + iSoluciones);
            nuevaSolucion=Solucion.crearSolucion();
            nuevaSolucion=Main.LocalSearch(nuevaSolucion, distanciaAlRio);
            nuevaSolucion=nuevaSolucion.factibilizar(100,100);

            if (nuevaSolucion.evaluarFitness() < mejorSolucion.evaluarFitness()) {
                mejoraActual=System.currentTimeMillis();
                if (verbose)System.out.println("GRASP-Actualizo el mejor.");
                if (verbose)System.out.println("GRASP-Mejor Solucion previa: "+ mejorSolucion.evaluarFitness());

                mejorSolucion = nuevaSolucion.clone();
                if (verbose)System.out.println("GRASP-Mejor Solucion actual: "+mejorSolucion.evaluarFitness());
                //Guardar en archivo el tiempo de ejecucion y la mejor solucion
                mejorSolucion.crearTimeLog(iSoluciones, ultimaMejora, mejoraActual);
                mejorSolucion.crearArchivoSolucion(iSoluciones, ultimaMejora, mejoraActual);
                ultimaMejora=mejoraActual;
                ultimaIteracion=iSoluciones;

            }else{
                if (verbose)System.out.println("GRASP-Conservo el mejor anterior ");
                if (verbose)System.out.println("GRASP-Mejor Solucion previa: "+ mejorSolucion.evaluarFitness());
            }
            if (verbose)System.out.println();
            if (verbose)System.out.println();
        }

        if (verbose)System.out.println("GRASP-Solucion Final: "+mejorSolucion.evaluarFitness());
        mejorSolucion.crearArchivoFitness();
        return mejorSolucion;

    }
    /**Se intenta modificar una solucion ejecutando fistImprove hasta que no mejore o llegue a la cantidad maxima
     * maxCantidadFI **/
    private static Solucion LocalSearch(Solucion solucion, boolean distanciaAlRio) {
        boolean huboMejora = true;
        int maxCantidadFI = Constantes.cantPixeles;//, UDprevia = 0;
        float pesoFosforo = 1, pesoProductividad = 1, pesoCantUsos = 1;
        Solucion solucionOriginal = solucion.clone();
        //System.out.println("\tLS-Solucion Original:");
        //solucion.imprimirFuncionObjetivo();

        //Hasta llegar a la cantidad maxima de iteraciones o no tener mejora
        for (int iSoluciones = 0; (iSoluciones < maxCantidadFI) && huboMejora; iSoluciones++) {
            //System.out.println("\tLS-Iteracion: " + (iSoluciones + 1));
            //Busco una mejora
            solucion = Solucion.firstImprove(solucion, pesoFosforo, pesoProductividad, pesoCantUsos, distanciaAlRio);
            //Comparo valores de la mejora
            if (solucionOriginal.evaluarFuncionObjetivo(pesoFosforo, pesoProductividad, pesoCantUsos)
                    > solucion.evaluarFuncionObjetivo(pesoFosforo, pesoProductividad, pesoCantUsos)) {
                //Obtube mejora
                //Muestro la mejora y los pesos
                //solucionOriginal.imprimirFuncionObjetivo(pesoFosforo, pesoProductividad, pesoCantUsos);
                //solucion.imprimirFuncionObjetivo(pesoFosforo, pesoProductividad, pesoCantUsos);
                //System.out.println();
                //Actualizo mi solucion original a la acutal
                solucionOriginal=solucion.clone();
                //Actualizo pesos
                pesoFosforo = Solucion.actualizarPesoFosforo(solucionOriginal, solucion, pesoFosforo);
                pesoProductividad = Solucion.actualizarPesoProduccion(solucionOriginal, solucion, pesoProductividad);
                pesoCantUsos = Solucion.actualizarPesoCantUsos(solucionOriginal, solucion, pesoCantUsos);


            } else {
                //No obtuve FirstImprovement
                huboMejora = false;
            }
        }

        /*
        System.out.println();
        System.out.println();
        System.out.println("Solucion antes de los LS:");
        System.out.println("\tSolucion original (con pesos): " + solucionOriginal.evaluarFuncionObjetivo(pesoFosforo, pesoProductividad, pesoCantUsos));
        System.out.println("\tSolucion original (sin pesos): " + solucionOriginal.evaluarFuncionObjetivo());
        System.out.println("\tSolucion final (con pesos): " + solucion.evaluarFuncionObjetivo(pesoFosforo, pesoProductividad, pesoCantUsos));
        System.out.println("\tSolucion final (sin pesos): " + solucion.evaluarFuncionObjetivo());
        solucion.chequearRestricciones();
        solucion.imprimirFuncionObjetivo();
        */

        return solucion;


    }
    /**Genera un archvio con los datos segun para la formulacion matematica opt_usos_v9.mod**/
    private static void generarArchvioDeDatos(int numProductor) {
        int  productor, pixel;
        Scanner reader = new Scanner(System.in);

        try {
            //Abro el archivo en moodo append
            PrintWriter archivo = new PrintWriter(new FileOutputStream(new File(("productor"+numProductor+".dat")), false /* append = true */) );
            archivo.println("#productor"+numProductor+".dat");
            //Usos enteros
            archivo.print("# Usos Enteros\nset K:=");
            for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                archivo.print(" "+Constantes.usos[iUso].numUso);
            }
            archivo.println(";");
            //Usos anuales
            archivo.print("# Usos Estacionario\nset I:=");
            for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                for (int iEstacion = 0; iEstacion < Constantes.usos[iUso].duracionEstaciones ; iEstacion++) {
                    archivo.print(" " + ((100 * Constantes.usos[iUso].numUso) + (iEstacion+ 1)));
                }
            }
            archivo.println(";");
            //Pixeles
            archivo.print("# Pixeles\nset J:=");
            for (int iPixel = 0; iPixel < Constantes.pixeles.length; iPixel++) {
                System.out.print("Pixel: "+iPixel);
                archivo.print(" "+Constantes.pixeles[iPixel].id);
            }
            archivo.println(";");
            //Productores
            archivo.print("# Productores\nset P:=");
            for (int iProductor = 0; iProductor< Constantes.productoresActivos.size(); iProductor++) {
                productor=Constantes.productoresActivos.get(iProductor);
                archivo.print(" "+(Constantes.productores[productor].numeroProductor+1));
            }
            archivo.println(";");
            //Estaciones a;o
            archivo.print("#Estaciones-año del período de planificación\nset D:=");
            for (int iEstacion = 0; iEstacion< Constantes.cantEstaciones; iEstacion++) {
                archivo.print(" "+ (iEstacion+1));
            }
            archivo.println(";");

            //Estaciones
            archivo.println("# estaciones (O,I,P,V)\nset E:= 1 2 3 4;");

            //Usos anuales del uso entero k
            archivo.print("\n#set IK{K} within I; Usos anuales del uso entero [k]");
            //Para cada uso entero,
            for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                // cargo sus usos anuales
                archivo.print("\nset IK["+Constantes.usos[iUso].numUso+"]:=");
                for (int iEstacion = 0; iEstacion < Constantes.usos[iUso].duracionEstaciones; iEstacion++) {
                    archivo.print(" " + ((100 * Constantes.usos[iUso].numUso) + (iEstacion + 1)));
                }
                /*
                if (Constantes.usos[iUso].primeraEstacion!=1) {
                    for (int iAno = 0; iAno < Math.ceil(Constantes.usos[iUso].duracionEstaciones / (float) 4); iAno++) {
                        archivo.print(" " + ((100 * Constantes.usos[iUso].numUso) + (iAno + 1)));
                    }
                }else{
                    for (int iAno = 0; iAno < Math.ceil((Constantes.usos[iUso].duracionEstaciones +2)/ (float) 4); iAno++) {
                        archivo.print(" " + ((100 * Constantes.usos[iUso].numUso) + (iAno + 1)));
                    }
                }*/
                archivo.print(";");
            }

            //Pixeles del productor p
            archivo.print("\n\n#set JP{P} within J; Pixeles del productor [p]");
            for (int iProductor = 0; iProductor< Constantes.productoresActivos.size(); iProductor++) {
                productor=Constantes.productoresActivos.get(iProductor);
                //Para cada productor
                archivo.print("\nset JP["+(Constantes.productores[productor].numeroProductor+1)+"]:= ");
                for (int iPixel = 0; iPixel< Constantes.productores[productor].pixelesDelProductor.size(); iPixel++) {
                    pixel=Constantes.productores[productor].pixelesDelProductor.get(iPixel);
                    archivo.print(" "+Constantes.pixeles[pixel].id);
                }
                archivo.print(";");
            }


            //Usos siguientes al uso anual i
            archivo.print("\n\n#set PI{I} within I;\t\t# usos estacionario Previo al uso estacionario i");
            //Calculo los previos de cada uso inicial

            List<List<Integer>> usosPrevios = new ArrayList<List<Integer> >(Constantes.usos.length);
            //Para cada uso creo una lista vacia con sus previos
            for (int iUso = 0; iUso < Constantes.usos.length; iUso++) {
                usosPrevios.add(new ArrayList<>());
                //System.out.println("Creo lista de previos de "+iUso);
            }
            //Para cada uso
            for (int iUsoPrevio = 0; iUsoPrevio < Constantes.usos.length; iUsoPrevio++) {
                //Recorro sus siguientes usos del uso previo y agrego el uso  previo a sus listas de previos
                //System.out.print("Agrego el uso "+iUsoPrevio +" com previo de los usos:");
                for (int iUsoSiguiente = 0; iUsoSiguiente < Constantes.usos[iUsoPrevio].siguientesUsos.size(); iUsoSiguiente ++) {
                    int usoSiguiente= Constantes.usos[iUsoPrevio].siguientesUsos.get(iUsoSiguiente);
                    //System.out.print(" "+usoSiguiente);
                    usosPrevios.get(usoSiguiente).add(iUsoPrevio*100+Constantes.usos[iUsoPrevio].duracionEstaciones);
                }
                //System.out.print("\n");
            }

            //Para cada uso
            for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                // cargo sus usos anuales
                for (int iEstacion = 0; iEstacion < Constantes.usos[iUso].duracionEstaciones; iEstacion++) {
                    int numEstacion=((100 * Constantes.usos[iUso].numUso) + (iEstacion)+1);
                    archivo.print("\nset PI["+numEstacion+"]:=");
                    if (iEstacion==0){
                        for (int iUsoSiguiente = 0; iUsoSiguiente < usosPrevios.get(Constantes.usos[iUso].numUso).size() ; iUsoSiguiente++) {
                            archivo.print(" "+ usosPrevios.get(Constantes.usos[iUso].numUso).get(iUsoSiguiente));
                        }

                    }else{
                        archivo.print(" " + (numEstacion-1));
                    }
                    archivo.print(";");

                }
            }

            archivo.print("\n\n#param DURACION >= 0 INTEGER;  Duracion en estaciones de la planificacion\nparam DURACION:="+Constantes.cantEstaciones+";");

            archivo.print("\n\n#param S{J} >= 0;  Superficie del pixel j\nparam S:=");
            for (int iPixel = 0; iPixel < Constantes.pixeles.length; iPixel++) {
                archivo.print(" "+Constantes.pixeles[iPixel].id+" "+Constantes.pixeles[iPixel].superficie);
                if(iPixel==(Constantes.pixeles.length-1)){
                    archivo.println(";");
                }else{
                    archivo.print(",");
                }
            }


            archivo.print("\n\n#param F{I} >= 0 Fósforo que exporta el uso anual i en la estación e" );
            archivo.print("\nparam F:= ");
            //Recorro cada I (cada uso anual)
            for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                for (int iEstacion = 0; iEstacion < Constantes.usos[iUso].duracionEstaciones; iEstacion++) {
                    archivo.print(" "+((100*Constantes.usos[iUso].numUso)+(iEstacion+1))+" "+Constantes.usos[iUso].fosforoEstacion[iEstacion]);
                    if ((iUso<Constantes.usos.length-1)||(iEstacion<Constantes.usos[iUso].duracionEstaciones-1)) {
                        archivo.print(",");
                    }else{
                        archivo.print(";");
                    }
                }
            }


            archivo.print("\n\n#param G{I} >= 0 Productividad del uso estacionario i, ya no depende de la estación e" );
            archivo.print("\nparam G:= ");
            //Recorro cada I (cada uso anual)
            for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                for (int iEstacion = 0; iEstacion < Constantes.usos[iUso].duracionEstaciones; iEstacion++) {
                    archivo.print(" "+((100*Constantes.usos[iUso].numUso)+(iEstacion+1))+" "+Constantes.usos[iUso].productividad[iEstacion]);
                    if ((iUso<Constantes.usos.length-1)||(iEstacion<Constantes.usos[iUso].duracionEstaciones-1)) {
                        archivo.print(",");
                    }else{
                        archivo.print(";");
                    }
                }
            }

            //param DUR{K} >= 0 integer;  duración del uso k en cantidad de estaciones-año
            archivo.print("\n\n#param DUR{K} >= 0 integer;  duración del uso k en cantidad de estaciones-año");
            archivo.print("\nparam DUR:=");
            //Para cada uso entero,
            for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                // cargo sus usos anuales
                archivo.print(" "+Constantes.usos[iUso].numUso+" "+Constantes.usos[iUso].duracionEstaciones);
                if (iUso!=Constantes.usos.length-1){
                    archivo.print(",");
                }else{
                    archivo.print(";\n");
                }
            }


            //param C{K,D} binary;		# indica si se puede comenzar el uso k en la estación-año d
            archivo.print("\n\n#param C{K,D} binary; # indica si se puede comenzar el uso k en la estación-año d");
            archivo.print("\nparam C: ");
            for (int iEstacion = 0; iEstacion< Constantes.cantEstaciones; iEstacion++) {
                archivo.print(" "+ (iEstacion+1));
            }
            archivo.print(":=		#Estaciones-año del período de planificación ");
            for (int iUso = 1; iUso < Constantes.cantUsos; iUso++) {
                archivo.print("\n"+Constantes.usos[iUso].numUso+" ");
                for (int iEstacion = 0; iEstacion <Constantes.cantEstaciones ; iEstacion++) {
                    if (Constantes.usos[iUso].primeraEstacion==0){
                        archivo.print(" " + ((iEstacion%4==0)? 1 : 0));
                    }else if(Constantes.usos[iUso].primeraEstacion==1) {
                        archivo.print(" " + ((iEstacion%4==2)? 1 : 0));
                    }else{
                        archivo.print(" 1");
                    }
                }
                if (iUso!= Constantes.cantUsos-1) {
                    archivo.print("");
                }else{
                    archivo.print(";\n");
                }
            }


            //param T{D} E;					# traduce de estación-año a estación
            archivo.print("\n#param T{D};\t\t# traduce de estación-año a estación\n");
            archivo.print("param T:=");
            for (int iEstacion = 0; iEstacion< Constantes.cantEstaciones; iEstacion++) {
                archivo.print(" "+(iEstacion+1)+" ");
                if (iEstacion%4==0){
                    archivo.print(" 1");
                }else if (iEstacion%4==1){
                    archivo.print(" 2");
                }else if (iEstacion%4==2){
                    archivo.print(" 3");
                }else if (iEstacion%4==3){
                    archivo.print(" 4");
                }
                if (iEstacion!=Constantes.cantEstaciones-1){
                    archivo.print(",");
                }
            }
            archivo.println(";		#traduce de estación-año a estación ");

            //#param MIN_USOS >= 0 integer;	# mínima cantidad de usos por productor por estación-año
            archivo.print("param MIN_USOS:= "+Constantes.minimaCantidadUsos+";\t# mínima cantidad de usos por productor por estación-año\n");
            //#param MAX_USOS >= 0 integer;	# máxima cantidad de usos por productor por estación-año
            archivo.print("param MAX_USOS:= "+Constantes.maximaCantidadUsos+";\t#  máxima cantidad de usos por productor por estación-año\n");
            //#param MIN_PROD{E} >= 0;			# mínima productividad por productor por estación
            archivo.print("param MIN_PROD:=  1 "+Constantes.restriccionProductividadProductorE[0]+
                    ", 2 "+Constantes.restriccionProductividadProductorE[1]+
                    ", 3 "+Constantes.restriccionProductividadProductorE[2]+
                    ", 4 "+Constantes.restriccionProductividadProductorE[3]+
                    ";\t#mínima productividad por productor por estación\n");


            archivo.print("\n\n#param USO_CERO{J, I} binary;	 indica si el usos estacionario I en el pixel J esta presente en el momento previo a la planificacion");
            archivo.print("\nparam USO_CERO:\n\t");
            for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                for (int iEstacion = 1; iEstacion <= Constantes.usos[iUso].duracionEstaciones; iEstacion++) {
                    archivo.print(((iUso*100)+iEstacion)+"\t");
                }
            }
            archivo.print(":=\n");
            for (int iPixel = 0; iPixel < Constantes.pixeles.length; iPixel++) {
                int usoBase=Uso.obtenerUsoBase(Constantes.pixeles[iPixel].usoOriginal);
                System.out.println("UsoOriginal: \""+ Constantes.pixeles[iPixel].usoOriginal +"\"\t\t Equivale= "+usoBase );
                archivo.print("\n"+Constantes.pixeles[iPixel].id+"\t");
                for (int iUso = 1; iUso < Constantes.usos.length; iUso++) {
                    for (int iEstacion = 1; iEstacion <= Constantes.usos[iUso].duracionEstaciones; iEstacion++) {

                        //System.out.println("Comparo: \""+ usoBase +"\" con \""+((iUso*100)+iEstacion)+"\"");

                        if( usoBase == ((iUso*100)+iEstacion) ){
                            archivo.print("1\t");
                        } else {
                            archivo.print("0\t");
                        }
                    }
                }
            }
            archivo.println(";");

            archivo.println("\nend;");
            archivo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        reader.close();
    }
    /**Interface para la linea de comando que ejecuta el programa, permite generar o cargar los archivos de usos,
     * especificar semilla, y cantidad de repeticiones, definir potreros o pixeles y definir fosforo o fosforo segun
     * distancia al rio**/
    private static void generarMejorSolucion() {
        int cantidadSoluciones = 0, datos;
        String fileName;
        Solucion solucion;
        Scanner reader = new Scanner(System.in);
        System.out.println("GRASP V0.7: ");
        System.out.print("Generar Archivos de uso (1 si, 2 no): ");
        datos = reader.nextInt();
        if (datos%2!=0) {
            Uso.crearArchivosUsos();
        }
        System.out.print("Cargar Archivos de uso (1 si, 2 no): ");
        datos = reader.nextInt();
        if (datos%2!=0) {
            Uso.cargarUsosDesdeArchivos();
        }

        System.out.print("Definir semilla: ");
        Constantes.uniforme=  new Random(reader.nextInt());

        System.out.print("Cantidad de soluciones a generar : ");
        cantidadSoluciones = reader.nextInt();

        System.out.print("1 para potreros, 2 para pixeles: ");
        datos = reader.nextInt();

        if (datos%2==0) {
            fileName = "pixeles.in";
            Constantes.cantPixeles = 18475;
            Constantes.cantPotreros = 26168;
        }else{
            fileName = "potreros.in";
            Constantes.cantPixeles = 792;
            Constantes.cantPotreros = 1602;
        }
        Constantes.pixeles = Pixel.cargarPixeles(fileName);

        System.out.print("1 calcular solo Fosforo, 2 calcular fosforo segun la Distancia al rio: ");
        datos = reader.nextInt();

        Constantes.mejorFosforo=new Solucion();
        Constantes.mejorFosforo.fosforo=Float.MAX_VALUE;
        Constantes.mejorCantIncumplimientoProductividad=new Solucion();
        Constantes.mejorCantIncumplimientoProductividad.restriccionProductividadMinimaEstacion.cantIncumplimientos
                =(int)Constantes.maximaCantidadIncumplimientoProductividadMinimaEstacion;
        Constantes.mejorCantIncumplimientoUsos=new Solucion();
        Constantes.mejorCantIncumplimientoUsos.restriccionUsosDistintos.cantIncumplimientos
                =(int)Constantes.maximoIncumplimientoUsosDistintos;


        solucion=Main.grasp(cantidadSoluciones, true, datos%2==0);
        solucion.recalcular();
        //Salida mejor solucion segun Funcion Objetivo
        Main.crearArchivos(solucion,"FuncionObjetivo");
        System.out.println("Mejor solucion FuncionObjetivo:");
        System.out.println("\tFosforo: " + solucion.fosforo);
        solucion.imprimirRestriccionProductividadMinimaEstacion();
        solucion.imprimirRestriccionUsosDistintos();
        //Genero archivos del mejor Fosforo
        Main.crearArchivos(Constantes.mejorFosforo, "Fosforo");
        System.out.println("Mejor Fosforo:");
        System.out.println("\tFosforo: " + Constantes.mejorFosforo.fosforo);
        Constantes.mejorFosforo.imprimirRestriccionProductividadMinimaEstacion();
        Constantes.mejorFosforo.imprimirRestriccionUsosDistintos();
        //Genero archivos del mejor Productividad
        Main.crearArchivos(Constantes.mejorCantIncumplimientoProductividad, "Productividad");
        System.out.println("Mejor Productividad:");
        System.out.println("\tFosforo: " + Constantes.mejorCantIncumplimientoProductividad.fosforo);
        Constantes.mejorCantIncumplimientoProductividad.imprimirRestriccionProductividadMinimaEstacion();
        Constantes.mejorCantIncumplimientoProductividad.imprimirRestriccionUsosDistintos();
        //Genero archivos del mejor Usos
        Main.crearArchivos(Constantes.mejorCantIncumplimientoUsos, "Usos");
        System.out.println("Mejor Usos:");
        System.out.println("\tFosforo: " + Constantes.mejorCantIncumplimientoUsos.fosforo);
        Constantes.mejorCantIncumplimientoUsos.imprimirRestriccionProductividadMinimaEstacion();
        Constantes.mejorCantIncumplimientoUsos.imprimirRestriccionUsosDistintos();


        reader.close();
    }
    /**Genera los archivos de salida  con el nombre brindado.**/
    private static void crearArchivos(Solucion solucion, String nombreSolucion) {
        solucion.crearArchivoMatriz(nombreSolucion);
        solucion.crearArchivoMatrizNombreUsoExtendido(nombreSolucion);
        solucion.crearArchivoCantidadUsos(nombreSolucion);
        solucion.crearArchivoProductividadSobreAreaTotal(nombreSolucion);
        solucion.crearArchivoFosforoSobreAreaTotal(nombreSolucion);
    }
    /**Test de FirstImprovement**/
    private static void testFirstImprovement(boolean distanciaAlRio) {
        Solucion solucionOriginal, solucionNueva;
        solucionOriginal = Solucion.crearSolucion();
        System.out.println("TEST FI\nORIGINAL:");
        solucionOriginal.imprimirFuncionObjetivo();
        System.out.println();

        for (int i = 0; i < 10000; i++) {
            System.out.println("Intento FI: " + i);
            solucionNueva = Solucion.firstImprove(solucionOriginal, 1, 1, 1, distanciaAlRio);
            if (solucionOriginal.evaluarFuncionObjetivo() < solucionNueva.evaluarFuncionObjetivo()) {
                System.out.println("\tFallo FI\tFOOriginal=" + solucionOriginal.evaluarFuncionObjetivo() + "\t FONueva=" + solucionNueva.evaluarFuncionObjetivo());
            } else {
                System.out.println("\tExito FI\tFOOriginal=" + solucionOriginal.evaluarFuncionObjetivo() + "\t FONueva=" + solucionNueva.evaluarFuncionObjetivo());
            }
        }
    }
    /**Test de localSearch**/
    private static void testLocalSearch(boolean distanciaAlRio) {
        Solucion solucionOriginal, solucionNueva;
        solucionOriginal = Solucion.crearSolucion();
        System.out.println("TEST LS\nORIGINAL:");
        solucionOriginal.imprimirFuncionObjetivo();
        System.out.println();

        for (int i = 0; i < 10; i++) {
            System.out.println("Intento LS: " + i);
            solucionNueva = Main.LocalSearch(solucionOriginal, distanciaAlRio);
            if (solucionOriginal.evaluarFuncionObjetivo() < solucionNueva.evaluarFuncionObjetivo()) {
                System.out.println("\tFallo LS\tFOOriginal=" + solucionOriginal.evaluarFuncionObjetivo() + "\t FONueva=" + solucionNueva.evaluarFuncionObjetivo());
            } else {
                System.out.println("\tExito LS\tFOOriginal=" + solucionOriginal.evaluarFuncionObjetivo() + "\t FONueva=" + solucionNueva.evaluarFuncionObjetivo());
            }
        }


    }
    /**Test de GRASP**/
    private static void testGRASP(boolean distanciaAlRio) {
        Solucion solucionOriginal, solucionNueva;
        solucionOriginal = Solucion.crearSolucion();
        System.out.println("TEST GRASP\nORIGINAL:");
        solucionOriginal.imprimirFuncionObjetivo();
        System.out.println();

        for (int i = 0; i < 10; i++) {
            System.out.println("Intento GRASP: " + i);
            solucionNueva = Main.grasp(10, true, distanciaAlRio);
            if (solucionOriginal.evaluarFuncionObjetivo() < solucionNueva.evaluarFuncionObjetivo()) {
                System.out.println("\tFallo GRASP\tFOOriginal=" + solucionOriginal.evaluarFuncionObjetivo() + "\t FONueva=" + solucionNueva.evaluarFuncionObjetivo());
            } else {
                System.out.println("\tExito GRASP\tFOOriginal=" + solucionOriginal.evaluarFuncionObjetivo() + "\t FONueva=" + solucionNueva.evaluarFuncionObjetivo());
            }
        }
    }
    /**TEST DE GRASPAlgEvo**/
    private static void testGRASPAlgEvo(boolean distanciaAlRio) {
        Constantes.cargarInstancia();
        Solucion solucionOriginal;
        solucionOriginal = Main.graspALgEvo(500000, 25000,true, distanciaAlRio);
        solucionOriginal.evaluarFitness();
    }


}


