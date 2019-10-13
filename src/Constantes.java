import java.util.*;

public class Constantes {
    //Para antonio
    public static int cantGrasp=1;
    public static int cantLS=1;

    //Luego pasar a globales
    public static int cantPixeles = 18475;
    public static int cantPotreros = 26168;

    /**/
    /*
    public static int cantPixeles = 792;
    public static int cantPotreros = 1602;
    */
    public static int cantEstaciones = 16;
    public static int cantAnios = cantEstaciones/4;
    public static int cantUsos= 15; //El cero esta reservado para inicio random
    public static int cantProductores= 42;
    public static int minimaCantidadUsos=3;
    public static int maximaCantidadUsos=6;

    //Maximo errores posibles para la funcion objetivo valor objetivo
    public static float maximoFosforoAnual=2.23F;
    public static float maximoIncumplimientoFosforo=maximoFosforoAnual*cantAnios;
    public static float maximoIncumplimientoProductividadMinimaEstacion= 2100;
    public static float maximaCantidadIncumplimientoProductividadMinimaEstacion=Constantes.cantEstaciones*Constantes.cantProductores;
    public static float maximoIncumplimientoUsosDistintos=Constantes.cantEstaciones*Constantes.cantProductores;

    //Parametros LS
    public static boolean actualizarPesosLSSiempre= false;
    public static boolean actualizarPesosLSConMejora= false;
    public static boolean actualizarPesosLSSinMejora= true;

    public static int minCantidadFI = 10;
    public static int maxCantidadFI = 15;
    public static int strikesFI = 3;

    //Parametros GRASP
    public static boolean actualizarPesosGRASPSiempre= false;
    public static boolean actualizarPesosGRASPConMejora= false;
    public static boolean actualizarPesosGRASPSinMejora= true;


    //TODO: Ver de eliminar estos pesos
    public static float pesoIncumplimientoFosforo=1;
    public static float pesoIncumplimientoProductividadMinimaEstacion= 1;
    public static float pesoIncumplimientoUsosDistintos=1;

    public static Map<String, Float> pesosProblema= new HashMap<>();
    static {
        pesosProblema = new HashMap<>();
        pesosProblema.put("fosforo", 1f);
        pesosProblema.put("productividad", 1f);
        pesosProblema.put("cantidadUsos", 1f);
    }
    public static Map<String, Float> pesosGRASP;
    static {
        pesosGRASP = new HashMap<>();
        pesosGRASP.put("fosforo", 1F);
        pesosGRASP.put("productividad", 1F);
        pesosGRASP.put("cantidadUsos", 1F);
    }public static Map<String, Float> pesosLS;
    static {
        pesosLS = new HashMap<>();
        pesosLS.put("fosforo", 1F);
        pesosLS.put("productividad", 1F);
        pesosLS.put("cantidadUsos", 1F);
    }
    /**Aumenta el peso que se le asigna al Fosforo al momento de evaluar la funcion objetivo**/
    //TODO: Parametriziar el aumento aumento como una variable de decision

    public static void definirPesosProblema (Float fosforo, Float productividad, Float cantUsos, Map<String, Float> peso){
        peso = new HashMap<>();
        peso.put("fosforo", fosforo);
        peso.put("productividad", productividad);
        peso.put("cantidadUsos", cantUsos);
    }


    public static float getPesoFosforoTotal() {
        return Constantes.pesosGRASP.get("fosforo")*Constantes.pesosLS.get("fosforo")*Constantes.pesosProblema.get("fosforo");
    }
    public static float getPesoProductividadTotal() {
        return Constantes.pesosGRASP.get("productividad")*Constantes.pesosLS.get("productividad")*Constantes.pesosProblema.get("productividad");
    }
    public static float getPesoCantUsosTotal() {
        return Constantes.pesosGRASP.get("cantidadUsos")*Constantes.pesosLS.get("cantidadUsos")*Constantes.pesosProblema.get("cantidadUsos");
    }

    public static Map<String, Float> actualizarPesos(Solucion solucionOriginal, Solucion solucion, Map<String, Float> peso) {
        //FOSFORO
        if (solucionOriginal.fosforo<solucion.fosforo){
            peso.put("fosforo", peso.get("fosforo")*1.1F);
        }else if (solucionOriginal.fosforo>solucion.fosforo){
            peso.put("fosforo", peso.get("fosforo")/1.1F);
        }
        //INCUMPLIMIENTO PRODUCTIVIDAD
        if (solucionOriginal.restriccionProductividadMinimaEstacion.cantIncumplimientos<solucion.restriccionProductividadMinimaEstacion.cantIncumplimientos){
            peso.put("productividad", peso.get("productividad")*1.1F);
        }else if (solucionOriginal.restriccionProductividadMinimaEstacion.cantIncumplimientos>solucion.restriccionProductividadMinimaEstacion.cantIncumplimientos){
            peso.put("productividad", peso.get("productividad")/1.1F);
        }else if (solucionOriginal.restriccionProductividadMinimaEstacion.cantIncumplimientos==0){
            peso.put("productividad", 1F);
        }
        //INCUMPLIMIENTO CANTUSOS
        if (solucionOriginal.restriccionUsosDistintos.cantIncumplimientos<solucion.restriccionUsosDistintos.cantIncumplimientos){
            peso.put("cantidadUsos", peso.get("cantidadUsos")*1.1F);
        }else if (solucionOriginal.restriccionUsosDistintos.cantIncumplimientos>solucion.restriccionUsosDistintos.cantIncumplimientos){
            peso.put("cantidadUsos", peso.get("cantidadUsos")/1.1F);
        }else if (solucionOriginal.restriccionUsosDistintos.cantIncumplimientos==0) {
            peso.put("cantidadUsos", 1F);
        }
        return peso;
    }

    public static void imprimirPesos(Map<String, Float> peso) {
        System.out.println("pFosforo: "+ peso.get("fosforo")+" pProductividad: "+ peso.get("productividad")+" pCantUsos: "+peso.get("cantidadUsos"));
    }

    public static Map<String, Float> inicializarPesos() {
        Map<String, Float> peso = new HashMap<>();
        peso.put("fosforo", 1F);
        peso.put("productividad", 1F);
        peso.put("cantidadUsos", 1F);
        return peso;
    }


        public static float mpp=1.0f;
    public static float[]  restriccionProductividadProductorE = new float[]
            {mpp*1200,mpp*600,mpp*2100,mpp*2100,mpp*1200,mpp*600,mpp*2100,mpp*2100,mpp*1200,mpp*600,mpp*2100,mpp*2100,mpp*1200,mpp*600,mpp*2100,mpp*2100};

    //TODO: Agregar las variables de decicion

    public static Uso[] usos = new Uso[cantUsos]; //el cero queda reservado para el no uso y el 14 cuando no se toca desde la ultima ves
    public static Pixel[] pixeles = new Pixel[cantPixeles];
    public static Productor[] productores= new Productor [cantProductores];
    public static int semilla=0;
    public static Random uniforme = new Random();
    public static List<Integer> productoresActivos= new ArrayList<Integer>();
    public static Solucion mejorFosforo;
    public static Solucion mejorCantIncumplimientoProductividad;
    public static Solucion mejorCantIncumplimientoUsos;



    /**Carga una instancia de problema desde el archivo ./Instancias/IntanciaPrueba.in**/
    public static void cargarInstancia() {
        String nombreInstancia = "./Instancias/IntanciaPrueba.in";
        cantPixeles = Pixel.contarLineas(nombreInstancia);
        System.out.println(nombreInstancia + "\tCantPixeles:" + cantPixeles);
        Constantes.cantPixeles = cantPixeles;
        Constantes.cantPotreros = cantPixeles;
        Constantes.productores = Productor.cargarProductores();
        Constantes.pixeles = Pixel.cargarPixeles(nombreInstancia);
        Constantes.usos = Uso.cargarUsos();
        Constantes.maximoIncumplimientoUsosDistintos = Constantes.cantEstaciones * Constantes.productoresActivos.size();
    }

    /**Carga solo un productor desde el archivo ./Instancias/IntanciaPrueba.in**/
    public static void cargarInstanciaProductor( int numProductor) {
        String nombreInstancia = "./Instancias/IntanciaPrueba.in";
        cantPixeles = Pixel.contarLineas(nombreInstancia);
        System.out.println(nombreInstancia + "\tCantPixeles:" + cantPixeles);
        Constantes.cantPixeles = cantPixeles;
        Constantes.cantPotreros = cantPixeles;
        Constantes.productores = Productor.cargarProductores();
        Constantes.pixeles = Pixel.cargarPixelesDeProductor(nombreInstancia, numProductor);
        Constantes.usos = Uso.cargarUsos();
        Constantes.maximoIncumplimientoUsosDistintos = Constantes.cantEstaciones * Constantes.productoresActivos.size();

    }


}




