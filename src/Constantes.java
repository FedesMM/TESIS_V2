import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Constantes {
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
    public static float pesoIncumplimientoFosforo=1;
    public static float pesoIncumplimientoProductividadMinimaEstacion= 1;
    public static float pesoIncumplimientoUsosDistintos=1;

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




