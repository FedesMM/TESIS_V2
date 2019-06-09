


import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Math.min;

class RestriccionProductividadProductores implements Cloneable{
    boolean cumpleRestriccion;
    float mediaDesviacion;
    float maximoDesviacion;
    int cantIncumplimientos;
    float incumplimientoRelativo;

    public RestriccionProductividadProductores() {
        this.cumpleRestriccion = false;
        this.cantIncumplimientos=0;
        this.mediaDesviacion = 0;
        this.maximoDesviacion = 0;
        this.incumplimientoRelativo=0;

    }

    public RestriccionProductividadProductores clone(){
        RestriccionProductividadProductores clon = new RestriccionProductividadProductores ();
        clon.cumpleRestriccion =this.cumpleRestriccion;
        clon.cantIncumplimientos=this.cantIncumplimientos;
        clon.mediaDesviacion=this.mediaDesviacion;
        clon.maximoDesviacion=this.maximoDesviacion;
        clon.incumplimientoRelativo=this.incumplimientoRelativo;
        return clon;
    }
}
class RestriccionUsosDistintos{
    boolean cumpleRestriccion;
    int cantIncumplimientos; //Cantidad de veces que una estacion un productor esta por debajo del minimo o encima del maximo
    float incumplimientoRelativo; //Cantidad de incumplimientos dividido entre todas posibles parejas productor,estacion
    int [][][] cantUsosPorEstacionParaCadaProductor;

    public RestriccionUsosDistintos() {
        this.cumpleRestriccion=false;
        this.cantIncumplimientos =0;
        this.incumplimientoRelativo=0;
        this.cantUsosPorEstacionParaCadaProductor =
                new int [Constantes.cantUsos][Constantes.cantEstaciones][Constantes.cantProductores];

    }

    public RestriccionUsosDistintos clone(){
        RestriccionUsosDistintos clon = new RestriccionUsosDistintos ();
        clon.cumpleRestriccion=this.cumpleRestriccion;
        clon.cantIncumplimientos=this.cantIncumplimientos;
        clon.incumplimientoRelativo=this.incumplimientoRelativo;
        clon.cantUsosPorEstacionParaCadaProductor=this.cantUsosPorEstacionParaCadaProductor.clone();
        return clon;
    }
}

public class Solucion {
    float fosforo;
    int[][] matriz; //Uso*100+numero de estacion de ese uso
    float [][] productivdadProductores;
    float[][] fosforoProductores;
    float areaTotal;

    //Resticcion 2 productividad minima por estacion para cada productor
    RestriccionProductividadProductores restriccionProductividadMinimaEstacion;
    //Restriccion 3 usos distintos, cuantos productores no la cumplen.
    RestriccionUsosDistintos restriccionUsosDistintos;

    /**Aumenta el peso que se le asigna al Fosforo al momento de evaluar la funcion objetivo**/
    //TODO: Parametriziar el aumento aumento como una variable de decision
    public static float actualizarPesoFosforo(Solucion solucionOriginal, Solucion solucion, float pesoFosforo) {

        if (solucionOriginal.fosforo<=solucion.fosforo){
            //No hubo mejora en fosforo actualizo el contador de cantidad de busquedas sin mejoras
            return (pesoFosforo*1.1F);
        }else{
            return (pesoFosforo/1.1F);
        }
    }

    /**Actualiza el peso que se le asigan al incumplimiento de la Productividad al momento de evaluar la funcion objetivo**/
    //TODO: Parametriziar el aumento aumento como una variable de decision
    public static float actualizarPesoProduccion(Solucion solucionOriginal, Solucion solucion, float pesoProduccion) {

        if (-solucionOriginal.restriccionProductividadMinimaEstacion.maximoDesviacion<=-solucion.restriccionProductividadMinimaEstacion.maximoDesviacion){
            //No hubo mejora en fosforo actualizo el contador de cantidad de busquedas sin mejoras
            return (pesoProduccion*1.1F);
        }else{
            return (pesoProduccion/1.1F);
        }
    }

    /**Actualiza el peso que se le asigna al incumplimiento de la CantUsos al momento de evaluar la funcion objetivo**/
    //TODO: Parametriziar el aumento aumento como una variable de decision
    public static float actualizarPesoCantUsos(Solucion solucionOriginal, Solucion solucion, float pesoCantUsos) {

        if (solucionOriginal.restriccionUsosDistintos.cantIncumplimientos<=solucion.restriccionUsosDistintos.cantIncumplimientos){
            //No hubo mejora en fosforo actualizo el contador de cantidad de busquedas sin mejoras
            return (pesoCantUsos*1.1F);
        }else{
            return (pesoCantUsos/1.1F);
        }
    }

    /**Convierte un genoma en una solucion**/
    public static Solucion genomaASolucion(int[] genoma) {
        Solucion solucion= new Solucion();
        //Calculo valores auxiliares
        for (int iPixel = 0; iPixel < Constantes.cantPixeles; iPixel++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                solucion.matriz[iPixel][iEstacion]=genoma[iPixel*Constantes.cantEstaciones+iEstacion];
            }
        }
        solucion.recalcular();//Incluye chequear restricciones.


        return solucion;
    }

    /**Convierte una solucion en un genoma**/
    public int[] solucionAGenoma() {
        int[] genoma = new int[Constantes.cantPixeles * Constantes.cantEstaciones];
        for (int iPixel = 0; iPixel < Constantes.cantPixeles; iPixel++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                genoma[iPixel*Constantes.cantEstaciones+iEstacion] = this.matriz[iPixel][iEstacion];
            }
        }

        return genoma;
    }

    /**Clona una solucion**/
    public Solucion clone(){
        Solucion clon = new Solucion ();
        clon.fosforo=this.fosforo;
        clon.matriz= this.matriz.clone(); //Uso*100+numero de estacion
        clon.productivdadProductores= this.productivdadProductores.clone();
        clon.fosforoProductores= this.fosforoProductores.clone();
        clon.areaTotal=this.areaTotal;
        clon.restriccionProductividadMinimaEstacion=this.restriccionProductividadMinimaEstacion.clone();
        clon.restriccionUsosDistintos=this.restriccionUsosDistintos.clone();
        return clon;
    }

    /**Imprime el valor de la funcion objetivo de esta solucion, tomando los pesos del fosforo, la productividad y
     * la cantIncumplimiento de Constantes.java **/
    public void imprimirFuncionObjetivo(){
        System.out.println("Funcion Objetivo: "+this.evaluarFuncionObjetivo());
        System.out.println("\tFosforo modulado: "+Constantes.pesoIncumplimientoFosforo * (this.fosforo /(Constantes.maximoIncumplimientoFosforo*this.areaTotal)));
        System.out.println("\tUsos Distintos modulado: "+ Constantes.pesoIncumplimientoUsosDistintos * (this.restriccionUsosDistintos.cantIncumplimientos/ Constantes.maximoIncumplimientoUsosDistintos));
        System.out.println("\tProductividad Estacion modulado: "+(-1)*Constantes.pesoIncumplimientoProductividadMinimaEstacion * (this.restriccionProductividadMinimaEstacion.maximoDesviacion /Constantes.maximoIncumplimientoProductividadMinimaEstacion));
    }

    /**Imprime el valor de la funcion objetivo de esta solucion segun los pesos dados**/
    public void imprimirFuncionObjetivo(float pesoFosforo, float pesoProductividad, float pesoCantUsos){
        System.out.println("Peso Fosforo: "+pesoFosforo+" Peso Productividad: "+pesoProductividad+" Peso CantUsos:"+pesoCantUsos);
        System.out.println("Funcion Objetivo: "+this.evaluarFuncionObjetivo(pesoFosforo,pesoProductividad,pesoCantUsos));
        System.out.print("\t("+pesoFosforo * (this.fosforo /(Constantes.maximoIncumplimientoFosforo*this.areaTotal))+",");
        System.out.print("\t"+(-1)*pesoProductividad * (this.restriccionProductividadMinimaEstacion.maximoDesviacion /Constantes.maximoIncumplimientoProductividadMinimaEstacion)+",");
        System.out.println("\t"+pesoCantUsos * (this.restriccionUsosDistintos.cantIncumplimientos/ Constantes.maximoIncumplimientoUsosDistintos)+")");

    }

    /**Devuelve el valor de la funcion objetivo de la solucion segun los pesos dados**/
    public float evaluarFuncionObjetivo(float pesoFosforo, float pesoProductividad, float pesoCantUsos){
        float valor=0;

        valor= pesoFosforo * (this.fosforo /(Constantes.maximoIncumplimientoFosforo*this.areaTotal));
        valor += - pesoProductividad * (this.restriccionProductividadMinimaEstacion.cantIncumplimientos /Constantes.maximaCantidadIncumplimientoProductividadMinimaEstacion);
        valor +=  pesoCantUsos * (this.restriccionUsosDistintos.cantIncumplimientos/ Constantes.maximoIncumplimientoUsosDistintos);
        return valor;
    }

    /**Devuelve el valor de la funcion objetivo de la solucion segun los pesos dados, tomando los
     * pesos de Constantes.java**/
    public float evaluarFuncionObjetivo(){
        float valor=0;
        valor= Constantes.pesoIncumplimientoFosforo * (this.fosforo /(Constantes.maximoIncumplimientoFosforo*this.areaTotal));
        valor += -Constantes.pesoIncumplimientoProductividadMinimaEstacion * (this.restriccionProductividadMinimaEstacion.cantIncumplimientos /Constantes.maximaCantidadIncumplimientoProductividadMinimaEstacion);
        valor +=  Constantes.pesoIncumplimientoUsosDistintos * (this.restriccionUsosDistintos.cantIncumplimientos/ Constantes.maximoIncumplimientoUsosDistintos);
        return valor;
    }

    /**Devuelve el valor de fosforo esportado por esta solucion, modulado por  el maximo fosforo posible y multiplicado
     * por el peso ambos definidios en Constantes.java**/
    public float evaluarFosforoModulado(){
        return Constantes.pesoIncumplimientoFosforo * (this.fosforo /(Constantes.maximoIncumplimientoFosforo*this.areaTotal));
    }

    /**Devuelve la media del incumplimiento de productividad, modulado por  el maximo incumplimiento posible y multiplicado
     * por el peso  ambos definidios en Constantes.java**/
    public float evaluarIncumplimientoProductividadModulado(){
        return -(this.restriccionProductividadMinimaEstacion.mediaDesviacion /Constantes.maximoIncumplimientoProductividadMinimaEstacion);
    }

    /**Devuelve la media del incumplimiento de cantUsos, modulado por  el maximo incumplimiento posible y multiplicado
     * por el peso  ambos definidios en Constantes.java**/
    public float evaluarIncumplimientoUsosDistintosModulado(){

        return this.restriccionUsosDistintos.cantIncumplimientos/ Constantes.maximoIncumplimientoUsosDistintos;
    }

    /**Crea una solucion vacia**/
    public Solucion (){
        this.fosforo=0;
        //Por la especificacion del lenguaje los array tienen valores 0  al inicializarce
        this.matriz = new int[Constantes.cantPixeles][Constantes.cantEstaciones]; //Uso*100+numero de estacion
        this.productivdadProductores = new float [Constantes.cantProductores][Constantes.cantEstaciones];
        this.fosforoProductores = new float [Constantes.cantProductores][Constantes.cantEstaciones];
        //Restriccion 2
        this.restriccionProductividadMinimaEstacion = new RestriccionProductividadProductores();
        //Restriccion 3
        this.restriccionUsosDistintos = new RestriccionUsosDistintos();
        this.areaTotal=0;
        for (int iProductores = 0; iProductores < Constantes.cantProductores; iProductores++) {
            this.areaTotal+=Constantes.productores[iProductores].areaTotal;
        }

    }

    /**Imprime en la linea de comandos los valores de la solucion**/
    public void imprimirSolucion(){
        System.out.println("\tSOLUCION:");
        System.out.println("\t\tFosforo Total: "+this.fosforo);
        this.imprimirMatriz();
        this.imprimirRestriccionProductividadMinimaEstacion();
        this.imprimirRestriccionUsosDistintos();
    }

    /**Imprime la planificacion de un pixel**/
    public void imprimirPixel(int pixel){
        System.out.printf("\t\t\tPixel "+pixel+": {");
        for (int estacion =0; estacion < this.matriz[pixel].length; estacion++){
            System.out.print(this.matriz[pixel][estacion]);
            if (estacion!=(this.matriz[pixel].length-1)){
                System.out.printf(", ");
            }
        }
        System.out.println("}");
    }

    /**Imprime todos la matriz con la planificacion de cada pixel**/
    public void imprimirMatriz(){
        System.out.println("\t\tMatriz:");
        for (int pixel =0; pixel< this.matriz.length;pixel++){
            System.out.printf("\t\t\tPixel "+pixel+": {");
            for (int estacion =0; estacion < this.matriz[pixel].length; estacion++){
                System.out.print(this.matriz[pixel][estacion]);
                if (estacion!=(this.matriz[pixel].length-1)){
                    System.out.printf(", ");
                }
            }
            System.out.println("}");
        }
    }

    /**Imprime los valores de la restriccion de Productividad por estacion de esta Solucion**/
    public void imprimirRestriccionProductividadMinimaEstacion(){
        System.out.println("\t\tRestriccion Productividad Minima Estacion: "+ this.restriccionProductividadMinimaEstacion.cumpleRestriccion);
        System.out.println("\t\t\tCant Incumplimientos: "+ this.restriccionProductividadMinimaEstacion.cantIncumplimientos);
        System.out.println("\t\t\tIncumplimiento relativo: "+ this.restriccionProductividadMinimaEstacion.incumplimientoRelativo);
        System.out.println("\t\t\tMaxima Desviacion: "+ this.restriccionProductividadMinimaEstacion.maximoDesviacion);
        System.out.println("\t\t\tMedia Desviacion: "+ this.restriccionProductividadMinimaEstacion.mediaDesviacion);
    }

    /**Imprime los valores de la restriccion de CantUsos por estacion de esta Solucion**/
    public void imprimirRestriccionUsosDistintos(){
        System.out.println("\t\tRestriccion Usos Distintos: "+ this.restriccionUsosDistintos.cumpleRestriccion);
        System.out.println("\t\t\tCant Incumplimientos: "+ this.restriccionUsosDistintos.cantIncumplimientos);
        System.out.println("\t\t\tIncumplimiento relativo: "+ this.restriccionUsosDistintos.incumplimientoRelativo);


    }

    /**Imprime los valores de la restriccion CantUsos de esta Solucion y una matriz con cantidad de usos por
     * estacion para cada productor**/
    public void imprimirRestriccionUsosDistintosExpandida(){

        System.out.println("\t\tRestriccion Usos Distintos: "+ this.restriccionUsosDistintos.cumpleRestriccion);
        System.out.println("\t\t\tCant Incumplimientos: "+ this.restriccionUsosDistintos.cantIncumplimientos);
        System.out.println("\t\t\tIncumplimiento relativo: "+ this.restriccionUsosDistintos.incumplimientoRelativo);
        imprimirUsosDisitintosPorEstacion();

    }

    /**Imprime una matriz con cantidad de usos por estacion para cada productor**/
    public void imprimirUsosDisitintosPorEstacion(){
        int cantUsos;

        System.out.println("\t\tUsos distintos por estacion:") ;
        for (int iProductor:Constantes.productoresActivos) {
            //for (int iProductor = 0; iProductor < Constantes.cantProductores ; iProductor++) {
            System.out.print("\t\t\tProductor "+iProductor+": { ") ;
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                cantUsos=0;
                for (int iUso = 0; iUso < Constantes.cantUsos; iUso++) {
                    if(this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[iUso][iEstacion][iProductor]>0){
                        cantUsos++;
                    }
                }
                if (iEstacion!=Constantes.cantEstaciones-1){
                    System.out.print(cantUsos+", ");
                }else{
                    System.out.println(cantUsos+"}") ;
                }
            }

        }
    }

    /**Imprime la productividad (dividida por su area total) de cada productor en cada estacion**/
    public void imprimirProductividadSobreSuperficiePorEstacion(){
        //Imprime la productividad (dividida por su area total) de cada productor en cada estacion
        System.out.println("\t\tProductividad Productores:");
        for (int productor:Constantes.productoresActivos) {
            //for (int productor =0; productor< this.productivdadProductores.length;productor++){
            System.out.printf("\t\t\tProductor "+productor+": {");
            for (int estacion =0; estacion < this.productivdadProductores[productor].length; estacion++){
                System.out.print(this.productivdadProductores[productor][estacion]/Constantes.productores[productor].areaTotal);
                if (estacion!=(this.productivdadProductores[productor].length-1)){
                    System.out.printf(", ");
                }
            }
            System.out.println("}");
        }
    }

    /**Crea una solucion creando pixeles al azar.**/
    public static Solucion crearSolucion(){
        Solucion solucion = new Solucion();
        //Cargar cada uno de los pixeles
        for (int iPixel=0; iPixel<Constantes.cantPixeles; iPixel++){
            solucion.cargarPixel(iPixel);
        }
        solucion.chequearRestricciones();
        return solucion;
    }

    /**Crea una solucion crando pixeles Factibles al azar **/
    public static Solucion crearSolucionFactible(){
        Solucion solucion = new Solucion();
        //Cargar cada uno de los pixeles
        for (int iPixel=0; iPixel<Constantes.cantPixeles; iPixel++){
            solucion.cargarPixelFactible(iPixel);
        }
        solucion.chequearRestricciones();
        return solucion;
    }

    /**Intenta mejorar (tantas veces como la Cantidad de Pixeles) un pixel al azar de la solucion  segun ciertos pesos para los criterios de la funcion objetivo**/
    public static Solucion firstImprove(Solucion solucion, float pesoFosforo, float pesoProductividad, float pesoCantUsos, boolean distanciaAlRio){
        Solucion respaldoSolucion=solucion.clone();
        int pixelRandom;

        for (int intentos = 0; intentos < Constantes.cantPixeles ; intentos++) {
            //Sorteo cual cambiar
            pixelRandom= Constantes.uniforme.nextInt(Constantes.cantPixeles-1);
            //Cambio el pixel sorteado
            solucion.cambiarPixel(pixelRandom, distanciaAlRio);
            solucion.recalcular(false, distanciaAlRio);
            //Chequeo contra los mejores
            if(solucion.fosforo<Constantes.mejorFosforo.fosforo){
                Constantes.mejorFosforo=solucion.clone();
                //System.out.println("Cambio mejor fosforo por solucion con: "+ solucion.fosforo);
            }
            if(solucion.restriccionProductividadMinimaEstacion.cantIncumplimientos <
                    Constantes.mejorCantIncumplimientoProductividad.restriccionProductividadMinimaEstacion.cantIncumplimientos){
                Constantes.mejorCantIncumplimientoProductividad=solucion.clone();
                //System.out.println("Cambio mejor productividad por solucion con: "+ solucion.restriccionProductividadMinimaEstacion.cantIncumplimientos);
            }
            if(solucion.restriccionUsosDistintos.cantIncumplimientos<
                    Constantes.mejorCantIncumplimientoUsos.restriccionUsosDistintos.cantIncumplimientos){
                Constantes.mejorCantIncumplimientoUsos=solucion.clone();
                //System.out.println("Cambio mejor usos por solucion con: "+ solucion.restriccionUsosDistintos.cantIncumplimientos);
            }

            //En caso de que me sirva lo devuelvo
            if (solucion.evaluarFuncionObjetivo(pesoFosforo,pesoProductividad,pesoCantUsos)< respaldoSolucion.evaluarFuncionObjetivo(pesoFosforo,pesoProductividad,pesoCantUsos)){
                //System.out.println("\t\tFI-Exito, con cantidad de fallos: "+fallos);
                return solucion;

            }else{
                //fallos++;
                solucion=respaldoSolucion.clone();
            }
        }
        //System.out.println("\t\tFI-Fracaso con cantidad de fallos: "+fallos);
        return respaldoSolucion;
    }

    /**Calcula cuanto se estan cumpliendo ambas restricciones**/
    public void chequearRestricciones(){
        this.cumpleRestriccionesProductividad();
        this.cumpleRestriccionUsosDistintos();
    }

    /**Cargo un uso sorteando segun siguienteUsoRuletaProduccion**/
    public  void cargarPixel(int iPixel){
        //Carga un nuevo pixel
        int iEstacion=0, iEstacionesCargadas=0, usoACargar, estacionActual, estacionesDeEsteUso, usoYDuracion[];
        usoYDuracion= new int[2];
        //System.out.println("Trabajo con el pixel: "+iPixel);

        //Relleno la estacion 0 del pixel
        String usoOriginal = Constantes.pixeles[iPixel].usoOriginal;
        //Averiguo que pixel tenia
        usoYDuracion=Uso.usoYDuracion(usoOriginal);
        //System.out.println("\tTengo que cargar por el uso original: "+usoYDuracion[0]);
        //System.out.println("\tme faltan : "+usoYDuracion[1]);
        //Completo las estaciones que me faltan
        while(usoYDuracion[1]>0){
            usoACargar=usoYDuracion[0];
            //Calculo la estacion del uso que voy a cargar
            estacionesDeEsteUso=Constantes.usos[usoACargar].duracionEstaciones-usoYDuracion[1];
            //Cargo el uso y la estacion
            this.matriz[iPixel][iEstacion]=100*usoACargar+estacionesDeEsteUso; //Antes usaba estacionActual pero seguro estaba mal
            //Aumento la cantidad de usos del due;o del pixel
            this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACargar][iEstacion][Constantes.pixeles[iPixel].productor]++;
            //Actualizo valores de la solucion
            /*
            System.out.print("\tIntento actualizar productividad del productor: "+Constantes.pixeles[iPixel].productor+" en la estacion "+iEstacion);
            System.out.print(" Aumentando su valor actual: "+this.productivdadProductores[Constantes.pixeles[iPixel].productor][iEstacion]);
            System.out.print(" segun los valores: "+Constantes.pixeles[iPixel].superficie);
            System.out.print(" usoACargar "+ usoACargar);
            System.out.print(" estacionesDeEsteUso "+ estacionesDeEsteUso);
            System.out.print(" y "+ Constantes.usos[usoACargar].productividad[estacionesDeEsteUso]);
            System.out.println(" Sumando: "+ Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeEsteUso]);
            */
            //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
            this.productivdadProductores[Constantes.pixeles[iPixel].productor][iEstacion] +=
                    Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeEsteUso];
            //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
            this.fosforoProductores[Constantes.pixeles[iPixel].productor][iEstacion] +=
                    Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso];
            //System.out.println("Actualizo valor: "+Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso]+
            //        "\t Acumulado:"+ this.fosforoProductores[Constantes.pixeles[iPixel].productor][iEstacion]+
            //        "\t Productor:"+ Constantes.pixeles[iPixel].productor+ "\t Estacion:"+iEstacion);
            //Actualizo lo que aporta el uso al fosforo total en esta estacion
            this.fosforo+=(Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie);
            //Aumento el iterador de iEstacion
            iEstacion++;
            //Reduzco la duracion
            usoYDuracion[1]--;
            iEstacionesCargadas++;
        }
        //Averiguo en que momento del plantio estaba
        //Si hay que llenar mas estaciones las lleno
        iEstacion=iEstacionesCargadas;

        usoACargar= Uso.siguienteUsoRuletaProduccion(usoYDuracion[0]);
        //System.out.println("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoYDuracion[0]+" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
        //Para un pixel recorro todas las estaciones
        while (iEstacion < Constantes.cantEstaciones){
            estacionesDeEsteUso=0;
            //Cargo todas las estaciones del uso, deteniendome si llego a cantEstaciones
            while((estacionesDeEsteUso<Constantes.usos[usoACargar].duracionEstaciones) && ((estacionesDeEsteUso+iEstacion)<(Constantes.cantEstaciones))){
                estacionActual=iEstacion+estacionesDeEsteUso;
                //Cargo el uso y las estaciones que llevaNo corresponde la duracion.
                this.matriz[iPixel][estacionActual]=100*usoACargar+estacionesDeEsteUso; //Antes usaba estacionActual pero seguro estaba mal
                //Aumento la cantidad de usos del due;o del pixel
                this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACargar][estacionActual][Constantes.pixeles[iPixel].productor]++;

                //Actualizo valores de la solucion
                //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                this.productivdadProductores[Constantes.pixeles[iPixel].productor][estacionActual]
                        += Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeEsteUso];
                //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                this.fosforoProductores[Constantes.pixeles[iPixel].productor][estacionActual]
                        += Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso];

                //Actualizo lo que aporta el uso al fosforo total en esta estacion
                this.fosforo+=(Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie);

                estacionesDeEsteUso++;
            }


            iEstacion=iEstacion + estacionesDeEsteUso; //Actualizo la siguiente estacion con la que trabajar
            //System.out.print("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoACargar);
            usoACargar= Uso.siguienteUsoRuletaProduccion(usoACargar); //Obtengo el siguiente uso a cargar
            //System.out.println(" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
        }
    }

    /**Carga un uso sorteando segun siguienteUsoRuletaProduccionCumpleCantUsos**/
    public  void cargarPixelFactible(int iPixel){
        //Carga un nuevo pixel
        int iEstacion=0, iEstacionesCargadas=0, usoACargar, estacionActual, estacionesDeEsteUso, usoYDuracion[], productor=Constantes.pixeles[iPixel].productor;
        ArrayList<Integer> usosDelProductorEstaEstacion;
        usoYDuracion= new int[2];
        //System.out.println("Trabajo con el pixel: "+iPixel);

        //Relleno la estacion 0 del pixel
        String usoOriginal = Constantes.pixeles[iPixel].usoOriginal;
        //Averiguo que pixel tenia
        usoYDuracion=Uso.usoYDuracion(usoOriginal);
        //System.out.println("\tTengo que cargar por el uso original: "+usoYDuracion[0]);
        //System.out.println("\tme faltan : "+usoYDuracion[1]);
        //Completo las estaciones que me faltan
        while(usoYDuracion[1]>0){

            usoACargar=usoYDuracion[0];
            //Calculo la estacion del uso que voy a cargar
            estacionesDeEsteUso=Constantes.usos[usoACargar].duracionEstaciones-usoYDuracion[1];
            //Cargo el uso y la estacion
            this.matriz[iPixel][iEstacion]=100*usoACargar+estacionesDeEsteUso; //Antes usaba estacionActual pero seguro estaba mal
            //Aumento la cantidad de usos del due;o del pixel
            this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACargar][iEstacion][productor]++;
            //Actualizo valores de la solucion
            /*
            System.out.print("\tIntento actualizar productividad del productor: "+productor+" en la estacion "+iEstacion);
            System.out.print(" Aumentando su valor actual: "+this.productivdadProductores[productor][iEstacion]);
            System.out.print(" segun los valores: "+Constantes.pixeles[iPixel].superficie);
            System.out.print(" usoACargar "+ usoACargar);
            System.out.print(" estacionesDeEsteUso "+ estacionesDeEsteUso);
            System.out.print(" y "+ Constantes.usos[usoACargar].productividad[estacionesDeEsteUso]);
            System.out.println(" Sumando: "+ Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeEsteUso]);
            */

            //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
            this.productivdadProductores[productor][iEstacion] +=
                    Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeEsteUso];
            //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
            this.fosforoProductores[productor][iEstacion] +=
                    Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso];
            //System.out.println("Actualizo valor: "+Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso]+
            //        "\t Acumulado:"+ this.fosforoProductores[productor][iEstacion]+
            //        "\t Productor:"+ productor+ "\t Estacion:"+iEstacion);
            //Actualizo lo que aporta el uso al fosforo total en esta estacion
            this.fosforo+=(Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie);

            //Aumento el iterador de iEstacion
            iEstacion++;
            //Reduzco la duracion
            usoYDuracion[1]--;
            iEstacionesCargadas++;
        }
        //Averiguo en que momento del plantio estaba
        //Si hay que llenar mas estaciones las lleno
        iEstacion=iEstacionesCargadas;
        usosDelProductorEstaEstacion= this.usosDelProductorPorEstacion(productor, iEstacion);
        usoACargar= Uso.siguienteUsoRuletaProduccionCumpleCantUsos(usoYDuracion[0],usosDelProductorEstaEstacion,productor);
        //System.out.println("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoYDuracion[0]+" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
        //Para un pixel recorro todas las estaciones
        while (iEstacion < Constantes.cantEstaciones){
            estacionesDeEsteUso=0;
            //Cargo todas las estaciones del uso, deteniendome si llego a cantEstaciones
            while((estacionesDeEsteUso<Constantes.usos[usoACargar].duracionEstaciones) && ((estacionesDeEsteUso+iEstacion)<(Constantes.cantEstaciones))){
                estacionActual=iEstacion+estacionesDeEsteUso;
                //Cargo el uso y las estaciones que llevaNo corresponde la duracion.
                this.matriz[iPixel][estacionActual]=100*usoACargar+estacionesDeEsteUso; //Antes usaba estacionActual pero seguro estaba mal
                //Aumento la cantidad de usos del due;o del pixel
                this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACargar][estacionActual][Constantes.pixeles[iPixel].productor]++;

                //Actualizo valores de la solucion
                //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                this.productivdadProductores[Constantes.pixeles[iPixel].productor][estacionActual]
                        += Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeEsteUso];
                //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                this.fosforoProductores[Constantes.pixeles[iPixel].productor][estacionActual]
                        += Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso];

                //Actualizo lo que aporta el uso al fosforo total en esta estacion
                this.fosforo+=(Constantes.usos[usoACargar].fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie);

                estacionesDeEsteUso++;
            }


            iEstacion=iEstacion + estacionesDeEsteUso; //Actualizo la siguiente estacion con la que trabajar
            //System.out.print("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoACargar);
            if (iEstacion<Constantes.cantEstaciones) {
                usosDelProductorEstaEstacion = this.usosDelProductorPorEstacion(productor, iEstacion);
                usoACargar = Uso.siguienteUsoRuletaProduccionCumpleCantUsos(usoYDuracion[0], usosDelProductorEstaEstacion, productor);
                //System.out.println(" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
            }
        }
    }

    /**Genera el Genoma de un pixel pudiendo sortear de que forma sortea el siguiente uso**/
    public static int [] crearGenomaPixel (int iPixel, int aleatorio){ //aleatorio=0 sortea por fosforo, aleatorio=1 sortea random, aleatorio>1 sotea productividad
        //creo el pixel a devolver
        int [] nuevoPixel=new int [Constantes.cantEstaciones];
        //Creo un un nuevo plan para el pixel
        int iEstacion=0, iEstacionesCargadas=0, usoACargar, estacionActual, estacionesDeEsteUso, usoYDuracion[];
        usoYDuracion= new int[2];
        //System.out.println("Trabajo con el pixel: "+iPixel);

        //Relleno la estacion 0 del pixel
        String usoOriginal = Constantes.pixeles[iPixel].usoOriginal;
        //Averiguo que pixel tenia
        usoYDuracion=Uso.usoYDuracion(usoOriginal);
        //System.out.println("\tTengo que cargar por el uso original: "+usoYDuracion[0]);
        //System.out.println("\tme faltan : "+usoYDuracion[1]);

        //En caso de que falten estaciones del uso heredado las completo
        while(usoYDuracion[1]>0){

            usoACargar=usoYDuracion[0];
            //Calculo la estacion del uso que voy a cargar
            estacionesDeEsteUso=Constantes.usos[usoACargar].duracionEstaciones-usoYDuracion[1];
            //Cargo el uso y la estacion
            nuevoPixel [iEstacion]=100*usoACargar+estacionesDeEsteUso;
            //Aumento el iterador de iEstacion
            iEstacion++;
            //Reduzco la duracion
            usoYDuracion[1]--;
            iEstacionesCargadas++;
        }
        //Averiguo en que momento del plantio estaba
        //Si hay que llenar mas estaciones las lleno
        iEstacion=iEstacionesCargadas;


        boolean sorteoPorFosforo= aleatorio==0 || Constantes.uniforme.nextFloat()<0.5F ;
        //usoACargar= Uso.siguienteUsoRuletaFosforo(usoYDuracion[0]);
        //usoACargar= Uso.siguienteUsoRuletaProduccion(usoYDuracion[0]);

        if (sorteoPorFosforo){
            usoACargar= Uso.siguienteUsoRuletaFosforo(usoYDuracion[0]);
        }else{
            usoACargar= Uso.siguienteUsoRuletaProduccion(usoYDuracion[0]);
        }

        //System.out.println("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoYDuracion[0]+" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
        //Para un pixel recorro todas las estaciones
        while (iEstacion < Constantes.cantEstaciones){
            estacionesDeEsteUso=0;
            //Cargo todas las estaciones del uso, deteniendome si llego a cantEstaciones
            while((estacionesDeEsteUso<Constantes.usos[usoACargar].duracionEstaciones) && ((estacionesDeEsteUso+iEstacion)<(Constantes.cantEstaciones))){
                estacionActual=iEstacion+estacionesDeEsteUso;
                //Cargo el uso y las estaciones que llevaNo corresponde la duracion.
                nuevoPixel[estacionActual]=100*usoACargar+estacionesDeEsteUso;
                estacionesDeEsteUso++;
            }
            iEstacion=iEstacion + estacionesDeEsteUso; //Actualizo la siguiente estacion con la que trabajar
            //System.out.print("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoACargar);
            if (iEstacion<Constantes.cantEstaciones) {
                if (sorteoPorFosforo){
                    usoACargar= Uso.siguienteUsoRuletaFosforo(usoACargar);
                }else{
                    usoACargar= Uso.siguienteUsoRuletaProduccion(usoACargar);
                }
            }
        }
        return nuevoPixel;

    }

    /**Modifica el Genoma de un pixel desde una estacion al azar**/
    public static int[] modificarGenomaPixel (int iPixel, int[] genomaPixel, int aleatorio){ //aleatorio=0 sortea por fosforo, aleatorio=1 sortea random, aleatorio>1 sotea productividad
        //Modifica un pixel desde alguna estacion

        int iEstacion=0,  usoACargar, estacionActual, usoPrevio, estacionesDeEsteUso, usoYDuracion[];
        usoYDuracion= new int[2];
        //System.out.println("Trabajo con el pixel: "+iPixel);

        //Averiguo que uso y duracion tenia
        String usoOriginal = Constantes.pixeles[iPixel].usoOriginal;
        //Averiguo que pixel tenia
        usoYDuracion=Uso.usoYDuracion(usoOriginal);
        //Sorteo una estacion a partir de la cual se va a buscar desde donde empezar a modificar, siempre es mayor que las estaciones del usoHeredado
        int estacionBase=usoYDuracion[1]+Constantes.uniforme.nextInt(Constantes.cantEstaciones-usoYDuracion[1]);
        //Recorro mi genoma hasta la primera estacion en que tenga un uso con estacionDeUso =1
        boolean encontre=false;
        for (int i = estacionBase; i >= usoYDuracion[1] && !encontre; i--) {
            if(genomaPixel[i]%100==0) {
                iEstacion = i;
                encontre=true;
            }
        }

        if(genomaPixel[iEstacion]%100!=0 ){
            System.out.println("No encuentro desde que estacion modificar Solucion.677");
            System.out.println("En el pixel: "+iPixel+" en la estacion:"+ iEstacion+" con el usoEstacionDeUso: " +genomaPixel[iEstacion]);
            System.exit(1);
        }
        if(iEstacion==0){
            usoPrevio= usoYDuracion[0];
        } else{
            usoPrevio=genomaPixel[iEstacion]/100;
        }

        boolean sorteoPorFosforo= aleatorio==0 || Constantes.uniforme.nextFloat()<0.5F ;
        //usoACargar= Uso.siguienteUsoRuletaFosforo(usoYDuracion[0]);
        //usoACargar= Uso.siguienteUsoRuletaProduccion(usoYDuracion[0]);

        if (sorteoPorFosforo){
            usoACargar= Uso.siguienteUsoRuletaFosforo(usoPrevio);
        }else{
            usoACargar= Uso.siguienteUsoRuletaProduccion(usoPrevio);
        }

        //System.out.println("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoYDuracion[0]+" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
        //Para un pixel recorro todas las estaciones
        while (iEstacion < Constantes.cantEstaciones){
            estacionesDeEsteUso=0;
            //Cargo todas las estaciones del uso, deteniendome si llego a cantEstaciones
            while((estacionesDeEsteUso<Constantes.usos[usoACargar].duracionEstaciones) && ((estacionesDeEsteUso+iEstacion)<(Constantes.cantEstaciones))){
                estacionActual=iEstacion+estacionesDeEsteUso;
                //Cargo el uso y las estaciones que llevaNo corresponde la duracion.
                genomaPixel[estacionActual]=100*usoACargar+estacionesDeEsteUso;
                estacionesDeEsteUso++;
            }
            iEstacion=iEstacion + estacionesDeEsteUso; //Actualizo la siguiente estacion con la que trabajar
            //System.out.print("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoACargar);
            if (iEstacion<Constantes.cantEstaciones) {
                if (sorteoPorFosforo){
                    usoACargar= Uso.siguienteUsoRuletaFosforo(usoACargar);
                }else{
                    usoACargar= Uso.siguienteUsoRuletaProduccion(usoACargar);
                }
            }
        }
        return genomaPixel;
    }

    /**Limpia un pixel restando sus aportes a la solucion**/
    public void limpiarPixel(int iPixel){
        //Libera un pixel actualizando las variables de restricciones
        //System.out.println("Limpiar pixel: "+iPixel);
        int usoABorrar, estacionesDeEsteUso;

        for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
            //100*usoACargar+estacionDelUso
            usoABorrar=this.matriz[iPixel][iEstacion]/100;
            estacionesDeEsteUso=this.matriz[iPixel][iEstacion]%100;

            //Libero el uso y las estaciones que lleva
            this.matriz[iPixel][iEstacion]=0; //Antes usaba estacionActual pero seguro estaba mal

            //Actualizo la cantidad de usos
            this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoABorrar][iEstacion][Constantes.pixeles[iPixel].productor]--;
            //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
            this.productivdadProductores[Constantes.pixeles[iPixel].productor][iEstacion] -=
                    Constantes.pixeles[iPixel].superficie * Constantes.usos[usoABorrar].productividad[estacionesDeEsteUso];
            //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
            this.fosforoProductores[Constantes.pixeles[iPixel].productor][iEstacion] -=
                    Constantes.pixeles[iPixel].superficie * Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeEsteUso];
            //Actualizo lo que aporta el uso al fosforo total en esta estacion
            this.fosforo-=(Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie);

        }
    }

    /**Recalcula todos los valores de la solucion para que sean coherentes con su matriz**/
    public void recalcular(){
        int usoACalcular, estacionesDeEsteUso;
        //Limpio valores
        this.fosforo=0;
        this.productivdadProductores= new float[Constantes.cantProductores][Constantes.cantEstaciones];
        this.fosforoProductores= new float[Constantes.cantProductores][Constantes.cantEstaciones];
        this.restriccionProductividadMinimaEstacion= new RestriccionProductividadProductores();
        this.restriccionUsosDistintos = new RestriccionUsosDistintos();

        for (int iPixel = 0; iPixel < Constantes.cantPixeles; iPixel++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                //100*usoACargar+estacionDelUso
                usoACalcular = this.matriz[iPixel][iEstacion] / 100;
                estacionesDeEsteUso = this.matriz[iPixel][iEstacion] % 100;

                //Actualizo lo que aporta el uso al fosforo total en esta estacion
                this.fosforo += (Constantes.usos[usoACalcular].fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie);
                //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                this.productivdadProductores[Constantes.pixeles[iPixel].productor][iEstacion] +=
                        Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACalcular].productividad[estacionesDeEsteUso];
                //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                this.fosforoProductores[Constantes.pixeles[iPixel].productor][iEstacion] +=
                        Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACalcular].fosforoEstacion[estacionesDeEsteUso];
                //Actualizo la cantidad de usos
                this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACalcular][iEstacion][Constantes.pixeles[iPixel].productor]++;
                //Actualizo fosforoAnual segun la estacion actual y el fosforo del Uso
            }
        }
        this.chequearRestricciones();
    }

    /**Recalcula, pudiendo imprimir, todos los valores de la solucion para que sean coherentes con su matriz**/
    public void recalcular(boolean imprimir, boolean distanciaAlRio){
        //this.imprimirMatriz();
        int usoACalcular, estacionesDeEsteUso;
        //Limpio valores
        this.fosforo=0;
        this.productivdadProductores= new float[Constantes.cantProductores][Constantes.cantEstaciones];
        this.fosforoProductores= new float[Constantes.cantProductores][Constantes.cantEstaciones];
        this.restriccionProductividadMinimaEstacion= new RestriccionProductividadProductores();
        this.restriccionUsosDistintos = new RestriccionUsosDistintos();

        for (int iPixel = 0; iPixel < Constantes.cantPixeles; iPixel++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                //100*usoACargar+estacionDelUso
                usoACalcular = this.matriz[iPixel][iEstacion] / 100;
                estacionesDeEsteUso = this.matriz[iPixel][iEstacion] % 100;
                Uso uso=Constantes.usos[usoACalcular];
                if (estacionesDeEsteUso>uso.fosforoEstacion.length){

                    System.out.println("Falla en el pixel "+iPixel+" en la estacion "+iEstacion+" guardado como:"+ this.matriz[iPixel][iEstacion]);
                    this.imprimirPixel(iPixel);
                    uso.imprimirUso();
                }
                //Actualizo lo que aporta el uso al fosforo total en esta estacion
                if (distanciaAlRio){
                    this.fosforo += (Constantes.usos[usoACalcular].fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie/Constantes.pixeles[iPixel].distanciaAlRio);
                }else{
                    this.fosforo += (Constantes.usos[usoACalcular].fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie);
                }

                if(imprimir){
                    System.out.println("Pixel: "+iPixel+" Estacion: "+iEstacion+" Fosforo: "+uso.fosforoEstacion[estacionesDeEsteUso]+ " Superficie: "+Constantes.pixeles[iPixel].superficie
                            +" Agrego: "+(uso.fosforoEstacion[estacionesDeEsteUso]*Constantes.pixeles[iPixel].superficie)+" FosforoAcumulado: "+this.fosforo);
                }
                //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                this.productivdadProductores[Constantes.pixeles[iPixel].productor][iEstacion] +=
                        Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACalcular].productividad[estacionesDeEsteUso];
                //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                this.fosforoProductores[Constantes.pixeles[iPixel].productor][iEstacion] +=
                        Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACalcular].fosforoEstacion[estacionesDeEsteUso];
                //Actualizo la cantidad de usos
                this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACalcular][iEstacion][Constantes.pixeles[iPixel].productor]++;
                //Actualizo fosforoAnual segun la estacion actual y el fosforo del Uso
            }
        }
        this.chequearRestricciones();
    }

    /**Modifica la planificacion de un pixel corrigiendo los valores de la solucion**/
    public  void cambiarPixel(int iPixel, boolean distanciaAlRio){
        //Toma un pixel ya cargado y lo cambia limpiando y actualizando variables en una sola recorrida
        int iEstacion=0, usoACargar, usoABorrar, estacionActual, estacionesDeUsoACargar, estacionesDeUsoABorrar, usoYDuracion[];
        usoYDuracion= new int[2];


        //NO debo modificar la continuacion del uso original ni variar lo que aporta a las restricciones


        //Relleno la estacion 0 del pixel
        String usoOriginal = Constantes.pixeles[iPixel].usoOriginal;
        //Averiguo que pixel tenia
        usoYDuracion=Uso.usoYDuracion(usoOriginal);
        //Calculo la primera estacion posterior al uso original
        iEstacion=usoYDuracion[1];
        //Calculo el siguiente uso a cargar
        usoACargar= Uso.siguienteUsoRuletaProduccion(usoYDuracion[0]);
        //Para un pixel recorro todas las estaciones
        while (iEstacion < Constantes.cantEstaciones){
            estacionesDeUsoACargar=0;
            //Cargo todas las estaciones del uso, deteniendome si llego a cantEstaciones
            while((estacionesDeUsoACargar<Constantes.usos[usoACargar].duracionEstaciones) && ((estacionesDeUsoACargar+iEstacion)<(Constantes.cantEstaciones))){
                //Calculo la estacion
                estacionActual=iEstacion+estacionesDeUsoACargar;
                //Obtengo el pixel a borrar
                usoABorrar=this.matriz[iPixel][estacionActual]/100;
                //Y la estacion de el uso
                estacionesDeUsoABorrar=this.matriz[iPixel][estacionActual]%100;

                //Cargo el uso y las estaciones que llevaNo corresponde la duracion.
                this.matriz[iPixel][estacionActual]=100*usoACargar+estacionesDeUsoACargar; //Antes usaba estacionActual pero seguro estaba mal

                //Actualizo valores de la solucion
                //En caso de ser necesario actualizo la cantidad de usos en en estaestacion en este pixel
                if (usoABorrar != usoACargar) {
                    //Actualizo la cantidad de usos
                    this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoABorrar][estacionActual][Constantes.pixeles[iPixel].productor]--;
                    this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACargar][estacionActual][Constantes.pixeles[iPixel].productor]++;
                    //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                    this.productivdadProductores[Constantes.pixeles[iPixel].productor][estacionActual] =
                            this.productivdadProductores[Constantes.pixeles[iPixel].productor][estacionActual]
                                    - Constantes.pixeles[iPixel].superficie * Constantes.usos[usoABorrar].productividad[estacionesDeUsoABorrar]
                                    + Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeUsoACargar];
                    //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                    this.fosforoProductores[Constantes.pixeles[iPixel].productor][estacionActual] =
                            this.fosforoProductores[Constantes.pixeles[iPixel].productor][estacionActual]
                                    - Constantes.pixeles[iPixel].superficie * Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeUsoABorrar]
                                    + Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeUsoACargar];
                    //Actualizo lo que aporta el uso al fosforo total en esta estacion
                    if (distanciaAlRio){
                        this.fosforo= this.fosforo
                                - (Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeUsoABorrar]*Constantes.pixeles[iPixel].superficie/Constantes.pixeles[iPixel].distanciaAlRio)
                                + (Constantes.usos[usoACargar].fosforoEstacion[estacionesDeUsoACargar]*Constantes.pixeles[iPixel].superficie/Constantes.pixeles[iPixel].distanciaAlRio);
                    }else{
                        this.fosforo= this.fosforo
                                - (Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeUsoABorrar]*Constantes.pixeles[iPixel].superficie)
                                + (Constantes.usos[usoACargar].fosforoEstacion[estacionesDeUsoACargar]*Constantes.pixeles[iPixel].superficie);
                    }

                }
                estacionesDeUsoACargar++;
            }
            iEstacion=iEstacion + estacionesDeUsoACargar; //Actualizo la siguiente estacion con la que trabajar
            //System.out.print("Pixel:"+ iPixel+" Estacion:"+ iEstacion+" Uso previo: "+usoACargar);
            usoACargar= Uso.siguienteUsoRuletaProduccion(usoACargar); //Obtengo el siguiente uso a cargar
            //System.out.println(" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
        }
    }

    /**Calcula el cumplimiento de la restriccion de productividad**/
    public void  cumpleRestriccionesProductividad(){
        float maximaDesviacionEstacion=0,mediaDesviacionEstacion=0, desviacion, productividadSobreSuperficie=0;
        int incumplimientoEstacion=0;
        this.restriccionProductividadMinimaEstacion.cumpleRestriccion =true;
        this.restriccionProductividadMinimaEstacion.mediaDesviacion=0;
        this.restriccionProductividadMinimaEstacion.maximoDesviacion=0;

        for (int iProductor:Constantes.productoresActivos) {
            //for (int iProductores = 0; iProductores < Constantes.cantProductores; iProductores++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                //RESTRICCION ESTACIONARIA
                productividadSobreSuperficie=this.productivdadProductores[iProductor][iEstacion]/Constantes.productores[iProductor].areaTotal;
                desviacion=min((productividadSobreSuperficie-Constantes.productores[iProductor].restriccionProduccionEstacion[iEstacion]),0);
                if (desviacion<0){
                    this.restriccionProductividadMinimaEstacion.cumpleRestriccion =false;
                    incumplimientoEstacion++;
                    mediaDesviacionEstacion+=desviacion;
                    maximaDesviacionEstacion=min(desviacion,maximaDesviacionEstacion);
                    //System.out.print("Desviacion estacion "+incumplimientoEstacion+": "+desviacion+" MediaAcumulada: "+mediaDesviacionEstacion+" maximaDesviacionEstacion: "+maximaDesviacionEstacion);
                    //System.out.println(" Productividad"+this.productivdadProductores[iProductores][iEstacion]+" Restriccion "+Constantes.productores[iProductores].restriccionProduccionEstacion[iEstacion]);
                }
            }
        }
        if (incumplimientoEstacion > 0){
            //Calculo la media de este productor
            mediaDesviacionEstacion=mediaDesviacionEstacion/incumplimientoEstacion;
            //Me quedo con la mejor de las dos
            this.restriccionProductividadMinimaEstacion.mediaDesviacion=mediaDesviacionEstacion;
            this.restriccionProductividadMinimaEstacion.maximoDesviacion=maximaDesviacionEstacion;
        }
        this.restriccionProductividadMinimaEstacion.cantIncumplimientos=incumplimientoEstacion;
        this.restriccionProductividadMinimaEstacion.incumplimientoRelativo=(float) this.restriccionProductividadMinimaEstacion.cantIncumplimientos/(float)(Constantes.cantEstaciones*Constantes.productoresActivos.size());

    }

    /**Calcula el cumplimiento  de la restriccion cantidad de usos.**/
    public  void cumpleRestriccionUsosDistintos(){
        int cantUsosDistintos;
        this.restriccionUsosDistintos.cumpleRestriccion=true;
        this.restriccionUsosDistintos.cantIncumplimientos=0;
        //Recorro Cada estacion
        for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
            //Recorro Cada Productor
            for (int iProductor:Constantes.productoresActivos) {
                //for (int iProductor = 0; iProductor < Constantes.cantProductores ; iProductor++) {
                cantUsosDistintos=0;
                //Calculo cuantos usos distintos tuvo
                for (int iUso = 0;  iUso< Constantes.cantUsos; iUso++) {
                    if (this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[iUso][iEstacion][iProductor] > 0){
                        cantUsosDistintos++;
                    }
                }
                if ((cantUsosDistintos < Constantes.productores[iProductor].getMinCantUsos()) ||
                        (cantUsosDistintos > Constantes.maximaCantidadUsos)){
                    this.restriccionUsosDistintos.cumpleRestriccion=false;
                    this.restriccionUsosDistintos.cantIncumplimientos++;
                }
            }
        }

        this.restriccionUsosDistintos.incumplimientoRelativo=(float) this.restriccionUsosDistintos.cantIncumplimientos/(float)(Constantes.cantEstaciones*Constantes.productoresActivos.size());
        /*System.out.println("this.restriccionUsosDistintos.cantIncumplimientos: "+this.restriccionUsosDistintos.cantIncumplimientos );
        System.out.println("Constantes.cantEstaciones: "+Constantes.cantEstaciones );
        System.out.println("Constantes.cantProductores: "+Constantes.cantProductores );
        System.out.println("this.restriccionUsosDistintos.incumplimientoRelativo: "+this.restriccionUsosDistintos.incumplimientoRelativo );
*/
    }

    /**Crea un archivo con la matriz de planificacion guardadolo con nombreSolucion como prefijo**/
    public void crearArchivoMatriz(String nombreSolucion){
        int uso=0;
        String[][] matriz;
        //Creo la matriz de potreros con valores en cero
        matriz = new String [Constantes.cantPotreros][Constantes.cantEstaciones];
        for (int iPotrero = 0; iPotrero < Constantes.cantPotreros; iPotrero++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++){
                //Paso a la siguiente estacionstacion++) {
                matriz[iPotrero][iEstacion]="Reservado";
            }
        }


        //Agrego los pixeles calculados
        for (int iPixel = 0; iPixel< Constantes.pixeles.length; iPixel++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                uso = this.matriz[iPixel][iEstacion] / 100;
                matriz[Constantes.pixeles[iPixel].id-1][iEstacion]=Constantes.usos[uso].nombre;
            }
        }
        //Creo el archivo
        try {
            PrintWriter writer = new PrintWriter(nombreSolucion+"-Matriz de Planificacion.out", "UTF-8");

            //Imprimo la primera fila que marca las estaciones
            writer.println("ID,N,10,0\tEst1\tEst2\tEst3\tEst4\tEst5\tEst6\tEst7\tEst8\tEst9\tEst10\tEst11\tEst12\tEst13\tEst14\tEst15\tEst16");
            //Para cada potrero
            for (int iPotrero = 0; iPotrero < Constantes.cantPotreros; iPotrero++) {
                //Imprimo los usos que se seleccionaron.
                writer.print((iPotrero+1));
                for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                    //Imprimo el uso de cada estacion
                    writer.print("\t" + matriz[iPotrero][iEstacion]);
                }
                writer.println();

            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**Crea un archivo en que guarda el valor de fitness de la solucion**/
    public void crearArchivoFitness(){

        try {
            //Abro el archivo en moodo append
            PrintWriter archivo = new PrintWriter(new FileOutputStream(new File("fitness.out"), true /* append = true */) );
            //Agrego el valor de fitness en una nueva linea
            //archivo.append(String.valueOf(this.evaluarFitness())+"\n");
            archivo.println(String.valueOf(this.evaluarFitness()));
            archivo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**Crea un archivo con la matriz de planificacion, con nombre de los usos, guardadolo con nombreSolucion como prefijo**/
    public void crearArchivoMatrizNombreUsoExtendido(String nombreSolucion){
        int uso=0;
        String[][] matriz;
        //Creo la matriz de potreros con valores en cero
        matriz = new String [Constantes.cantPotreros][Constantes.cantEstaciones];
        for (int iPotrero = 0; iPotrero < Constantes.cantPotreros; iPotrero++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                matriz[iPotrero][iEstacion]="Reservado";
            }
        }


        //Agrego los pixeles calculados
        for (int iPixel = 0; iPixel< Constantes.pixeles.length; iPixel++) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                uso = this.matriz[iPixel][iEstacion] / 100;
                matriz[Constantes.pixeles[iPixel].id-1][iEstacion]=Constantes.usos[uso].nombre;
                if (uso<8){
                    matriz[Constantes.pixeles[iPixel].id-1][iEstacion]+=Uso.getEstacionUso(this.matriz[iPixel][iEstacion]%100);
                }
            }
        }
        //Creo el archivo
        try {
            PrintWriter writer = new PrintWriter(nombreSolucion+ "-Matriz de Planificacion Nombre Extendido.out", "UTF-8");

            //Imprimo la primera fila que marca las estaciones
            writer.println("ID,N,10,0\tEst1\tEst2\tEst3\tEst4\tEst5\tEst6\tEst7\tEst8\tEst9\tEst10\tEst11\tEst12\tEst13\tEst14\tEst15\tEst16");
            //Para cada potrero
            for (int iPotrero = 0; iPotrero < Constantes.cantPotreros; iPotrero++) {
                //Imprimo los usos que se seleccionaron.
                writer.print((iPotrero+1));
                for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                    //Imprimo el uso de cada estacion
                    writer.print("\t" + matriz[iPotrero][iEstacion]);
                }
                writer.println();

            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**Crea un archivo con una matriz de estacion por productor con la cantidad de usos distintos en cada celda**/
    public void crearArchivoCantidadUsos(String nombreSolucion){
        int cantUsos;
        try {
            //Creo y abro el archivo
            PrintWriter writer = new PrintWriter(nombreSolucion+"-Cantidad de Usos Distintos por Estacion.out", "UTF-8");

            //Imprimo la primera fila que marca las estaciones
            writer.println("Productor.C.254\tEst1\tEst2\tEst3\tEst4\tEst5\tEst6\tEst7\tEst8\tEst9\tEst10\tEst11\tEst12\tEst13\tEst14\tEst15\tEst16");
            //Para cada productor
            for (int iProductor:Constantes.productoresActivos) {
                //for (int iProductor = 0; iProductor < Constantes.cantProductores ; iProductor++) {
                writer.print(iProductor+"\t");
                //Recorro cada estacio
                for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                    //cantIncumplimientos=0;
                    cantUsos=0;
                    //Recorro cada Uso, sumando todos los usos distintos que usoque uso
                    for (int iUso = 0; iUso < Constantes.cantUsos; iUso++) {
                        if(this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[iUso][iEstacion][iProductor]>0){
                            cantUsos++;
                        }
                    }
                    //Imprimo la cantidad de usos distintos para ese productor en esa estacion
                    writer.print(cantUsos+"\t");
                }
                //Salto de linea entre productores
                writer.println();
            }
            //Cierro el archivo
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**Crea un archivo con una matriz de estacion por productor con la productividad sobre area del productor en cada celda**/
    public void crearArchivoProductividadSobreAreaTotal(String nombreSolucion){
        float total=0, totalConSuperficie=0;
        try {
            //Creo y abro el archivo
            PrintWriter writer = new PrintWriter(nombreSolucion+"-Productividad sobre Area Total por Estacion.out", "UTF-8");
            //Imprimo la primera fila que marca las estaciones
            writer.println("Productor.C.254\tEst1\tEst2\tEst3\tEst4\tEst5\tEst6\tEst7\tEst8\tEst9\tEst10\tEst11\tEst12\tEst13\tEst14\tEst15\tEst16\tTotal sin Superficie\tTotal con Superficie");
            //Para cada productor
            for (int iProductor:Constantes.productoresActivos) {
                //for (int iProductor =0; iProductor< this.productivdadProductores.length;iProductor++){
                total=0;
                writer.print(iProductor+"\t");
                //Recorro cada estacion imprimo la productividad sobre el Area total
                for (int estacion =0; estacion < this.productivdadProductores[iProductor].length; estacion++){
                    total+=this.productivdadProductores[iProductor][estacion]/Constantes.productores[iProductor].areaTotal;
                    totalConSuperficie+=this.productivdadProductores[iProductor][estacion];
                    writer.print(this.productivdadProductores[iProductor][estacion]/Constantes.productores[iProductor].areaTotal+"\t");
                }
                writer.println(total+"\t"+totalConSuperficie);
            }
            //Cierro el archivo
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**Crea un archivo con una matriz de estacion por productor con la exportacion de fosforo en cada celda**/
    public void crearArchivoFosforoSobreAreaTotal(String nombreSolucion){
        float total=0, totalConSuperficie=0;
        try {
            //Creo y abro el archivo
            PrintWriter writer = new PrintWriter(nombreSolucion+"-Fosforo sobre Area Total por Estacion.out", "UTF-8");
            //Imprimo la primera fila que marca las estaciones
            writer.println("Productor.C.254\tEst1\tEst2\tEst3\tEst4\tEst5\tEst6\tEst7\tEst8\tEst9\tEst10\tEst11\tEst12\tEst13\tEst14\tEst15\tEst16\tTotal sin Superficie\tTotal con Superficie");
            //Para cada productor
            for (int iProductor:Constantes.productoresActivos) {
                //for (int iProductor =0; iProductor< this.fosforoProductores.length;iProductor++){
                total=0;
                totalConSuperficie=0;
                writer.print(iProductor+"\t");
                //Recorro cada estacion imprimo la productividad sobre el Area total
                for (int estacion =0; estacion < this.fosforoProductores[iProductor].length; estacion++){
                    writer.print(this.fosforoProductores[iProductor][estacion]/Constantes.productores[iProductor].areaTotal+"\t");
                    total+=this.fosforoProductores[iProductor][estacion]/Constantes.productores[iProductor].areaTotal;
                    totalConSuperficie+=this.fosforoProductores[iProductor][estacion];
                }
                writer.println(total+"\t"+totalConSuperficie);
            }
            //Cierro el archivo
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**Intenta factibililzar una solucion, sorteando pixeles que podrian estar desfactibilizando por CantDeUsos**/
    public boolean factibilizarCantUsos(){
        int estacionOriginal, posibleUso;
        //for (int iProductor = 0; iProductor< Constantes.cantProductores; iProductor++) {
        for (int iProductor: Constantes.productoresActivos) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                //Calculo la cantidad de usos distintos para este productor en esta estacion.
                ArrayList<Integer> usosDelProductorEstaEstacion= this.usosDelProductorPorEstacion(iProductor, iEstacion);
                //Si me faltan usos:
                if (usosDelProductorEstaEstacion.size()<Constantes.productores[iProductor].getMinCantUsos()){
//                    System.out.print("Productor"+iProductor+" Estacion "+iEstacion+": ");
//                    System.out.println("\tMe faltan usos");
                    //Averiguo cuantos cambiar
                    int cantCambios= Constantes.productores[iProductor].getMinCantUsos()-usosDelProductorEstaEstacion.size();
                    //Cambio la cantidad necesaria
                    for (int iCambios = 0; iCambios < cantCambios; iCambios++) {

                        //Sorteo entre los pixeles del productor el pixel con menos EstacioneDeUso de un uso con un Uso usado al menos dos veces.
                        int pixelACambiar= sortearPixelDeProductorParaFactibilizarPorUsos(iEstacion, iProductor);
                        int estacionDelUso= matriz[pixelACambiar][iEstacion]%100;
                        //Cambio en el pixel de esta estacion en adelante por un uso que aun no tenga
                        //System.out.println("pixelACambiar: "+pixelACambiar+"\tiEstacion-estacionDelUso: "+(iEstacion-estacionDelUso));
                        boolean factible = this.corregirPixelSegunCantUsos(pixelACambiar, iEstacion-estacionDelUso, usosDelProductorEstaEstacion);
                        for (int cantIter = 0; cantIter < 1000 && !factible; cantIter++) {
                            factible = this.corregirPixelSegunCantUsos(pixelACambiar, iEstacion-estacionDelUso, usosDelProductorEstaEstacion);
                        }
                    }
                }
                //Si me sobran sorteo un tipo de uso ruleta invertida segun cuantos pixeles tenga
                else if(usosDelProductorEstaEstacion.size()>Constantes.maximaCantidadUsos){
//                    System.out.print("Productor"+iProductor+" Estacion "+iEstacion+": ");
//                    System.out.println("\tMe sobran usos");
                    //System.out.println("Estacion "+iEstacion+": ");
                    //Averiguo cuantos cambiar
                    //int cantCambios = usosDelProductorEstaEstacion.size() -Constantes.maximaCantidadUsos;
                    //Armo una lista de los usos que me voy a quedar,
//                    System.out.println("\tArmo lista de usos a concervar: ");
                    // Primero me quedo con los que son previos a la estacon cero
                    ArrayList<Integer> usosAConservar= new ArrayList<>();
                    //int i=0;
//                    System.out.print("\t\tAgrego usos heredados: ");
                   for (int iPixel: Constantes.productores[iProductor].pixelesDelProductor) {
                        estacionOriginal= iEstacion-(matriz[iPixel][iEstacion]%100);
                        posibleUso =matriz[iPixel][iEstacion]/100;
                        if (estacionOriginal<0 && !usosAConservar.contains(posibleUso)) {
                            usosAConservar.add(posibleUso);
//                            System.out.print(" "+posibleUso);
                        }
                    }
//                    System.out.println();
                    //Alerto si la instancia es no factible, si tengo mas usos distintos previos a la estacion cero
                    if (usosAConservar.size()>Constantes.maximaCantidadUsos){
                        //System.out.println("INSTANCIA NO VALIDA!!!  Tengo mas usos distintos previos a la estacion cero");
                        return false; //TODO evaluar esto de mejor forma
                    }
                    //System.out.print("\t\tAgrego uso no heredados: ");
                    for (int iUso: usosDelProductorEstaEstacion) {
                        if (!usosAConservar.contains(iUso) && usosAConservar.size()<Constantes.maximaCantidadUsos){
//                            System.out.print(" "+iUso);
                            usosAConservar.add(iUso);
                        }

                    }
//                    System.out.println();

                    //Recorro todos los pixeles del productor, cambiando los que no tengan usos de la lista a conservar
                    for (Integer iPixel: Constantes.productores[iProductor].pixelesDelProductor) {
                        int estacionDelUso= matriz[iPixel][iEstacion]%100;
                        if ( !usosAConservar.contains(matriz[iPixel][iEstacion]/100)){
                            //System.out.println("pixelACambiar: "+iPixel+"\tiEstacion-estacionDelUso: "+(iEstacion-estacionDelUso));
                            boolean factible = this.corregirPixelSegunCantUsos(iPixel, iEstacion-estacionDelUso, usosAConservar);
                            for (int cantIter = 0; cantIter < 1000 && !factible; cantIter++) {
                                factible = this.corregirPixelSegunCantUsos(iPixel, iEstacion-estacionDelUso, usosAConservar);
                            }
                        }
                    }
                }else{
                    //System.out.println("System.out.print(\"Productor \"+iProductor+\" Estacion \"+iEstacion+\":\");Me faltan usos");
                    //System.out.println("\tCorrecto");
                }
            }
        }
        return true;
    }

    /**Devuelve una lista deshordenada de los distintos usos que tiene en sus pixeles un productor.**/
    private ArrayList<Integer> usosDelProductorPorEstacion(int iProductor, int iEstacion) {
        ArrayList<Integer> usosDelProductorEstaEstacion= new ArrayList<>();
        for (int iUso = 0; iUso < Constantes.cantUsos; iUso++) {
            //System.out.println("[iEstacion] "+iEstacion+" [iProductor] "+iProductor+" [iUso] "+iUso);
            if (this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[iUso][iEstacion][iProductor]>0){
                if(!usosDelProductorEstaEstacion.contains(iUso)){
                    usosDelProductorEstaEstacion.add(iUso);
                }
            }
        }
        Collections.shuffle(usosDelProductorEstaEstacion);
        return usosDelProductorEstaEstacion;
    }

    /**Creo un nuevo pixel desde una estacion en adelante sorteando los siguientes usos segun Cant de usos**/
    private boolean corregirPixelSegunCantUsos(int iPixel, int estacionOriginal, ArrayList<Integer> usosDelProductorEstaEstacion) {
        //Toma un pixel ya cargado y lo cambia limpiando y actualizando variables en una sola recorrida
        int iEstacion=estacionOriginal, usoACargar=0, usoABorrar, estacionActual, estacionesDeUsoACargar, estacionesDeUsoABorrar, usoYDuracion[];
        int productor= Constantes.pixeles[iPixel].productor;
        usoYDuracion= new int[2];

        if (estacionOriginal <0 ) {
            //System.out.println("INSTANCIA NO VALIDA!!! En corregirPixelSegunCantUsos");
            //TODO EVALUAR ESTO MEJOR
            //System.exit(1);
            return false;
        }else if(estacionOriginal==0) {
            //Obtengo el valor de Uso original
            //String usoOriginal = Constantes.pixeles[iPixel].usoOriginal;
            //Averiguo que uso y duracion tenia
            usoYDuracion = Uso.usoYDuracion(Constantes.pixeles[iPixel].usoOriginal);
            //Calculo la primera estacion posterior al uso original
            iEstacion = usoYDuracion[1];
            //Si tengo al menos un uso siguiente que corrija el Pixel
            if(Constantes.usos[usoYDuracion[0]].tengoSiguiente(usosDelProductorEstaEstacion)){
                //Sorteo un uso que lo corrija
                usoACargar=Uso.siguienteUsoRuletaFosforoCumpleCantUsos(usoYDuracion[0], usosDelProductorEstaEstacion, productor);
            }else{
                //Devuelvo que no lo pude corregir
                System.out.println("No pude corregir. iPixel: "+iPixel+" iEstacion: "+iEstacion);
                return false;
            }
        }else{//estacionOriginal>0
            //System.out.println("Entro a siguienteUsoRuletaFosforoCumpleCantUsos.");
            usoACargar = Uso.siguienteUsoRuletaFosforoCumpleCantUsos(this.matriz[iPixel][estacionOriginal-1]/100, usosDelProductorEstaEstacion, productor);
            //System.out.println("Salgo de siguienteUsoRuletaFosforoCumpleCantUsos.");
        }
        //Para un pixel recorro todas las estaciones
        while (iEstacion < Constantes.cantEstaciones){
            //System.out.println("Comienzo a modificar el iPixel "+iPixel+" en la iEstacion "+iEstacion+" un usoACargar "+usoACargar);
            estacionesDeUsoACargar=0;
            //Cargo todas las estaciones del uso, deteniendome si llego a cantEstaciones
            while((estacionesDeUsoACargar<Constantes.usos[usoACargar].duracionEstaciones) && ((estacionesDeUsoACargar+iEstacion)<(Constantes.cantEstaciones))){
                //Calculo la estacion
                estacionActual=iEstacion+estacionesDeUsoACargar;
                //En caso de ser necesario actualizo la cantidad de usos en en estaestacion en este pixel
                if (this.matriz[iPixel][estacionActual] != (100*usoACargar+estacionesDeUsoACargar)) {
                    //Obtengo el pixel a borrar
                    usoABorrar=this.matriz[iPixel][estacionActual]/100;
                    //Y la estacion de el uso
                    estacionesDeUsoABorrar=this.matriz[iPixel][estacionActual]%100;
                    //Cargo el uso y las estaciones que llevaNo corresponde la duracion.
                    this.matriz[iPixel][estacionActual]=100*usoACargar+estacionesDeUsoACargar; //Antes usaba estacionActual pero seguro estaba mal
                    //Actualizo la cantidad de usos
                    this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoABorrar][estacionActual][productor]--;
                    this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACargar][estacionActual][productor]++;
                    //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                    this.productivdadProductores[productor][estacionActual] -=
                            Constantes.pixeles[iPixel].superficie * Constantes.usos[usoABorrar].productividad[estacionesDeUsoABorrar]
                                    + Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeUsoACargar];
                    //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                    this.fosforoProductores[productor][estacionActual] -=
                            Constantes.pixeles[iPixel].superficie * Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeUsoABorrar]
                                    + Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeUsoACargar];
                    //Actualizo lo que aporta el uso al fosforo total en esta estacion
                    this.fosforo= this.fosforo
                            - (Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeUsoABorrar]*Constantes.pixeles[iPixel].superficie)
                            + (Constantes.usos[usoACargar].fosforoEstacion[estacionesDeUsoACargar]*Constantes.pixeles[iPixel].superficie);
                }
                estacionesDeUsoACargar++;
            }
            iEstacion=iEstacion + estacionesDeUsoACargar; //Actualizo la siguiente estacion con la que trabajar
            if (iEstacion<16) {
                usoACargar = Uso.siguienteUsoRuletaFosforoCumpleCantUsos(usoACargar, usosDelProductorPorEstacion(productor, iEstacion), productor);
            }
            //Uso.siguienteUsoRuletaProduccion(usoACargar); //Obtengo el siguiente uso a cargar
            //System.out.println(" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
        }
        return true;
    }

    /**Sortea un pixel de un productor que pueda estar dando problemas en la estacion iEstacion por la cantidad de usos**/
    public int sortearPixelDeProductorParaFactibilizarPorUsos(int iEstacion, int iProductor) {
        List<Integer> pixelACambiar= new ArrayList<>();
        int minEstacion = Constantes.cantEstaciones;
//        System.out.println("iEstacion "+iEstacion+" iProductor: "+iProductor);
//        System.out.print("\tCant de usos por estacion para este productor:");
//        for (int iUso = 0; iUso < Constantes.cantUsos; iUso++) {
//            System.out.print(" "+this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[iUso][iEstacion][iProductor]);
//
//        }
//        System.out.println();

        //Busco entre los pixeles del producto el pixel con menos EstacioneDeUso de un uso con un Uso usado al menos dos veces.
//        System.out.println("Sorteo para iEstacion: "+iEstacion+"\tiProductor: "+iProductor);
        for (Integer iPixel: Constantes.productores[iProductor].pixelesDelProductor) {
//            System.out.print("\tiPixel: "+iPixel);
            //Consigo el uso y la estacion del actual
            int usoDelPixel=this.matriz[iPixel][iEstacion]/100;
            int estacionesDelUso= this.matriz[iPixel][iEstacion]%100;
//            System.out.print("\tusoDelPixel: "+usoDelPixel+"\testacionDelUso: "+estacionesDelUso);
//            System.out.println("\tCantidad Veces Usados: "+this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoDelPixel][iEstacion][iProductor]);
            //Si es un uso, usado al menos 2 veces en esta estacion por este productor
            if (this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoDelPixel][iEstacion][iProductor]>1){
                //Si su estacion del uso es menor que el pixel a cambiar actual
                if (estacionesDelUso< minEstacion){
                    minEstacion = estacionesDelUso;
                    pixelACambiar= new ArrayList<>();
                    pixelACambiar.add(iPixel);
                } else if (estacionesDelUso == minEstacion) {
                    pixelACambiar.add(iPixel);
                }
            }
        }
        if (pixelACambiar.size()>1){
            return pixelACambiar.get(Constantes.uniforme.nextInt(pixelACambiar.size() - 1)); // TODO: puede explotar si la lista es vacia
        } else{
            //No tengo pixeles en la lista le doy uno cualquiera
            return Constantes.productores[iProductor].pixelesDelProductor.get(Constantes.uniforme.nextInt(Constantes.productores[iProductor].pixelesDelProductor.size()));
        }



    }

    /**Intenta factibililzar una solucion, sorteando pixeles que podrian estar desfactibilizando por Productividad**/
    public void factibilizarProductividad() {
        int pixelACambiar, estacionDelUso;
//        System.out.println("factibilizarProductividad: ");
        //for (int iProductor = 0; iProductor< Constantes.cantProductores; iProductor++) {
        for (int iProductor: Constantes.productoresActivos) {
            for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                //Calculo la cantidad de usos distintos para este productor en esta estacion.
                ArrayList<Integer> usosDelProductorEstaEstacion= this.usosDelProductorPorEstacion(iProductor, iEstacion);
                //Si me faltan falta productividad:
                if (this.productivdadProductores[iProductor][iEstacion]/Constantes.productores[iProductor].areaTotal
                        <Constantes.productores[iProductor].restriccionProduccionEstacion[iEstacion]){
//                    System.out.println("\tIncumplimiento para el produtor "+iProductor+" en la estacion "+ iEstacion
//                            + ": "+this.productivdadProductores[iProductor][iEstacion]+"/"+Constantes.productores[iProductor].restriccionProduccionEstacion[iEstacion]);
                    int maxCambios=Constantes.productores[iProductor].pixelesDelProductor.size();
                    for (int iCambios = 0; ((iCambios < maxCambios)&&
                    (this.productivdadProductores[iProductor][iEstacion]/Constantes.productores[iProductor].areaTotal
                    <Constantes.productores[iProductor].restriccionProduccionEstacion[iEstacion])); iCambios++) {
                        //Sorteo un pixel del productor ponderando la productividad de esta estacion
                        pixelACambiar= this.sortearPixelDeProductorParaFactibilizarProductividad(iEstacion, iProductor);
                        estacionDelUso=this.matriz[pixelACambiar][iEstacion]%100;
                        //Corrijo el pixel sorteado
                        this.corregirPixelSegunProductividad(pixelACambiar, iEstacion-estacionDelUso, usosDelProductorEstaEstacion);
                    }
                } else{
//                    System.out.println("\tCumplimiento para el produtor "+iProductor+" en la estacion "+ iEstacion
//                            + ": "+this.productivdadProductores[iProductor][iEstacion]+"/"+Constantes.productores[iProductor].restriccionProduccionEstacion[iEstacion]);
                }

            }
        }
    }

    /**Sortea un pixel de un productor que pueda estar dando problemas en la estacion iEstacion por la Productividad**/
    private int sortearPixelDeProductorParaFactibilizarProductividad(int iEstacion, int iProductor) {
        //Obtengo la produccion total del productor en esta estacion
        float produccionTotal= 0, produccionInvertidaTotal=0, sorteo=0, acumulado=0;
        int  uso,estacionUso;
        List<Integer> posiblesPixeles= new ArrayList<>();
        HashMap<Integer, Float> produccionInvertida=new HashMap();
//        System.out.println("Entro a Sortear. Productividad total: "+produccionTotal);

//        System.out.print("\tArmo lista de pixeles: ");

        //Calculo la productividad total de esta estacion
        for (int iPixel:Constantes.productores[iProductor].pixelesDelProductor) {
            uso=this.matriz[iPixel][iEstacion]/100;
            estacionUso=this.matriz[iPixel][iEstacion]%100;
            produccionTotal+=Constantes.usos[uso].productividad[estacionUso];
        }
        //Cargo los pixeles no heredados
        for (int iPixel:Constantes.productores[iProductor].pixelesDelProductor) {
            uso=this.matriz[iPixel][iEstacion]/100;
            estacionUso=this.matriz[iPixel][iEstacion]%100;
            if(iEstacion-estacionUso>=0){
                posiblesPixeles.add(iPixel);
                //Calculo la produccion invertida para cada pixel y la total
                produccionInvertida.put(iPixel,(produccionTotal-Constantes.usos[uso].productividad[estacionUso]));
                //Aumento la Productividad invertida total
                produccionInvertidaTotal+=produccionInvertida.get(iPixel);
//                System.out.print(iPixel+" ");
            }
        }

//        System.out.println();
        if (posiblesPixeles.size()>0){
            if(produccionTotal>0){
                //Sorteo un numero entre cero y la productividad invertida total
                sorteo= Constantes.uniforme.nextFloat()*produccionInvertidaTotal;
//            System.out.println("\tSortie: "+sorteo);
                //Averiguo a que pixel corresponde el sorteo
//            System.out.print("\tBusco el pixel correspondiente: "
                for (Integer iPixel:produccionInvertida.keySet()) {
//                System.out.print(iPixel+" ");
                    acumulado+=produccionInvertida.get(iPixel);
                    if (sorteo<acumulado){
//                    System.out.println("\n\tSortie el pixel: "+iPixel);
                        return iPixel;
                    }
                }
            }else {
                //System.out.println("Pixel sin productividad");
                return Constantes.uniforme.nextInt(posiblesPixeles.size());
            }

            System.out.print("NO ENCONTRE EL PIXEL SORTEADO EN: estacion "+iEstacion+" productor" + iProductor);
            System.out.print("\tPosibles Pixeles:" +posiblesPixeles.toString()+ " ProduccionInvertida "+produccionInvertida.values().toString());
            System.out.println("\tSorteado:"+sorteo+" ProductividadInvertidaTotal: "+produccionInvertidaTotal+" Acumulado"+acumulado+" ProductividadTotal"+produccionTotal);
            this.imprimirMatriz();

        }else {
            System.out.println("No hay pixel que sortear en estacion "+iEstacion+" para el "+ iProductor);
        }
        return 0;
    }

    /**Crea un nuevo pixel desde una estacion en adelante sorteando los siguientes usos segun Productividad**/
    private boolean corregirPixelSegunProductividad(int iPixel, int estacionOriginal, ArrayList<Integer> usosDelProductorEstaEstacion) {
        //Toma un pixel ya cargado y lo cambia limpiando y actualizando variables en una sola recorrida
        int iEstacion=estacionOriginal, usoACargar=0, usoABorrar, estacionActual, estacionesDeUsoACargar, estacionesDeUsoABorrar, usoYDuracion[];
        int productor= Constantes.pixeles[iPixel].productor;

        if (estacionOriginal <0 ) {
//            System.out.print("INSTANCIA NO VALIDA!!! En: corregirPixelSegunProductividad");
            return false;
        }else if(estacionOriginal==0) {
            //Obtengo el valor de usoOriginal del pixel
            String usoOriginal = Constantes.pixeles[iPixel].usoOriginal;
            //Averiguo que uso tenia
            usoYDuracion = Uso.usoYDuracion(usoOriginal);
            //Calculo la primera estacion posterior al uso original
            iEstacion = usoYDuracion[1];
            //Si tengo al menos un uso siguiente que corrija el Pixel
            if(Constantes.usos[usoYDuracion[0]].tengoSiguiente(usosDelProductorEstaEstacion)){
                //Sorteo un uso que lo corrija
                usoACargar=Uso.siguienteUsoRuletaProduccionCumpleCantUsos(usoYDuracion[0], usosDelProductorEstaEstacion, productor);
            }else{
                //Devuelvo que no lo pude corregir
                System.out.println("No pude corregir. iPixel: "+iPixel+" iEstacion: "+iEstacion);
                return false;
            }
        }else{//estacionOriginal>0
            //System.out.println("Entro a siguienteUsoRuletaFosforoCumpleCantUsos.");
            usoACargar = Uso.siguienteUsoRuletaProduccionCumpleCantUsos(this.matriz[iPixel][estacionOriginal-1]/100, usosDelProductorEstaEstacion, productor);
            //System.out.println("Salgo de siguienteUsoRuletaFosforoCumpleCantUsos.");
        }
        //Para un pixel recorro todas las estaciones
        while (iEstacion < Constantes.cantEstaciones){
            //System.out.println("Comienzo a modificar el iPixel "+iPixel+" en la iEstacion "+iEstacion+" un usoACargar "+usoACargar);
            estacionesDeUsoACargar=0;
            //Cargo todas las estaciones del uso, deteniendome si llego a cantEstaciones
            while((estacionesDeUsoACargar<Constantes.usos[usoACargar].duracionEstaciones) && ((estacionesDeUsoACargar+iEstacion)<(Constantes.cantEstaciones))){
                //Calculo la estacion
                estacionActual=iEstacion+estacionesDeUsoACargar;
                //En caso de ser necesario actualizo la cantidad de usos en en estaestacion en este pixel
                if (this.matriz[iPixel][estacionActual] != (100*usoACargar+estacionesDeUsoACargar)) {
                    //Obtengo el pixel a borrar
                    usoABorrar=this.matriz[iPixel][estacionActual]/100;
                    //Y la estacion de el uso
                    estacionesDeUsoABorrar=this.matriz[iPixel][estacionActual]%100;
                    //Cargo el uso y las estaciones que llevaNo corresponde la duracion.
                    this.matriz[iPixel][estacionActual]=100*usoACargar+estacionesDeUsoACargar; //Antes usaba estacionActual pero seguro estaba mal
                    //Actualizo la cantidad de usos
                    this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoABorrar][estacionActual][productor]--;
                    this.restriccionUsosDistintos.cantUsosPorEstacionParaCadaProductor[usoACargar][estacionActual][productor]++;
                    //Actualizo la productividad del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                    this.productivdadProductores[productor][estacionActual] -=
                            Constantes.pixeles[iPixel].superficie * Constantes.usos[usoABorrar].productividad[estacionesDeUsoABorrar]
                                    + Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].productividad[estacionesDeUsoACargar];
                    //Actualizo el fosforo del productor due;o del pixel segun la superficie del pixel y la productividad del uso para la estacion del uso
                    this.fosforoProductores[productor][estacionActual] -=
                            Constantes.pixeles[iPixel].superficie * Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeUsoABorrar]
                                    + Constantes.pixeles[iPixel].superficie * Constantes.usos[usoACargar].fosforoEstacion[estacionesDeUsoACargar];
                    //Actualizo lo que aporta el uso al fosforo total en esta estacion
                    this.fosforo= this.fosforo
                            - (Constantes.usos[usoABorrar].fosforoEstacion[estacionesDeUsoABorrar]*Constantes.pixeles[iPixel].superficie)
                            + (Constantes.usos[usoACargar].fosforoEstacion[estacionesDeUsoACargar]*Constantes.pixeles[iPixel].superficie);

                }
                estacionesDeUsoACargar++;
            }
            iEstacion=iEstacion + estacionesDeUsoACargar; //Actualizo la siguiente estacion con la que trabajar
            if (iEstacion<16) {
                usoACargar = Uso.siguienteUsoRuletaProduccionCumpleCantUsos(usoACargar, usosDelProductorPorEstacion(productor, iEstacion), productor);
            }
            //Uso.siguienteUsoRuletaProduccion(usoACargar); //Obtengo el siguiente uso a cargar
            //System.out.println(" Siguiente uso: "+usoACargar+" Estaciones a cargar: "+Constantes.usos[usoACargar].duracionEstaciones);
        }
        return true;
    }

    /**Chequea si ambas restricciones se estan cumpliendo**/
    public boolean esFactible() {
        return (this.restriccionUsosDistintos.cumpleRestriccion && this.restriccionProductividadMinimaEstacion.cumpleRestriccion);
    }

    /**Factibiliza hasta tantas veces como repeticiones*profundidad**/
    public Solucion factibilizar(int repeticiones, int profundidad) {
        Solucion copia=this.clone();
        for (int iRepeticion = 0; iRepeticion < repeticiones && !this.esFactible(); iRepeticion++) {
            for (int iProfundidad = 0; iProfundidad < profundidad && !this.esFactible(); iProfundidad++) {
                if (!copia.restriccionUsosDistintos.cumpleRestriccion) {
                    copia.factibilizarCantUsos();
                }
                if (!copia.restriccionProductividadMinimaEstacion.cumpleRestriccion) {
                    copia.factibilizarProductividad();
                }
                copia.recalcular();
                if (copia.esFactible()){
                    return copia;
                }
            }
            copia=this.clone();
        }
        return this;
    }

    /**Devuelve el fitness de la solucion**/
    public float evaluarFitness() {
        return this.fosforo + this.restriccionProductividadMinimaEstacion.cantIncumplimientos * Constantes.maximoIncumplimientoFosforo
                + this.restriccionUsosDistintos.cantIncumplimientos * Constantes.maximoIncumplimientoFosforo;

    }

    /**Imprime el fitness de la solcuion**/
    public void imprimirFitness(){
        System.out.println("Funcion Objetivo: "+this.evaluarFitness());
        System.out.println("\tFosforo: "+this.fosforo);
        System.out.println("\tIncumplimiento Usos Distintos: "+ this.restriccionProductividadMinimaEstacion.cantIncumplimientos * Constantes.maximoIncumplimientoFosforo);
        System.out.println("\tIncumplimiento Productividad Estacion: "+(-1)*this.restriccionUsosDistintos.cantIncumplimientos * Constantes.maximoIncumplimientoFosforo);
    }

    /**Creo o agrego al archivo time.out el tiempo actual y cuanto mejora en fitness desde la ultima mejor solucion**/
    public void crearTimeLog(int iSolucion, long ultimaMejora, long mejoraActual) {
        try {
            //Abro el archivo en moodo append
            PrintWriter archivo = new PrintWriter(new FileOutputStream(new File("time.out"), true /* append = true */) );
            //Agrego el valor de fitness en una nueva linea
            //archivo.append(String.valueOf(this.evaluarFitness())+"\n");
            archivo.println(iSolucion+"\t"+String.valueOf(System.currentTimeMillis())+"\t"+String.valueOf(mejoraActual-ultimaMejora)+"\t"+String.valueOf(this.evaluarFitness()));
            archivo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**Crea un archivo con los valores mas importantes de la solucion y su matriz de planificacion y cuanto tiempo lleva desde la ultima mejor solucion**/
    public void crearArchivoSolucion(int iSolucion, long ultimaMejora, long mejoraActual){
        //Creo el archivo
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(new File("soluciones.out"), true /* append = true */));
            writer.println(iSolucion+"\t"+String.valueOf(System.currentTimeMillis())+"\t"+String.valueOf(mejoraActual-ultimaMejora)+"\t"+String.valueOf(this.evaluarFitness()));
            //Imprimo la primera fila que marca las estaciones
            writer.println("\tID,N,10,0\tEst1\tEst2\tEst3\tEst4\tEst5\tEst6\tEst7\tEst8\tEst9\tEst10\tEst11\tEst12\tEst13\tEst14\tEst15\tEst16");
            //Para cada potrero
            for (int iPotrero = 0; iPotrero < Constantes.cantPotreros; iPotrero++) {
                //Imprimo los usos que se seleccionaron.
                writer.print("\t"+(iPotrero+1));
                for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                    //Imprimo el uso de cada estacion
                    writer.print("\t" + this.matriz[iPotrero][iEstacion]);
                }
                writer.println();

            }
            writer.println();
            writer.println();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**Crea un archivo con los valores mas importantes de la solucion y su matriz de planificacion **/
    public void crearArchivoPlanificacion( String nombreArchivo) {
        //Creo el archivo
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(new File(nombreArchivo), true /* append = true */));
            writer.println("Fosforo total:= "+ this.fosforo);
            writer.println("Restriccion Productividad:= "+this.restriccionProductividadMinimaEstacion.cantIncumplimientos);
            writer.println("Restriccion CantUsos:= "+this.restriccionUsosDistintos.cantIncumplimientos);
            writer.println("Pixeles := Planificacion");
            //Para cada potrero
            for (int iPotrero = 0; iPotrero < Constantes.cantPixeles; iPotrero++) {
                //Imprimo los usos que se seleccionaron.
                writer.print("  "+(Constantes.pixeles[iPotrero].id)+":=");
                for (int iEstacion = 0; iEstacion < Constantes.cantEstaciones; iEstacion++) {
                    //Imprimo el uso de cada estacion
                    writer.print(" " + (this.matriz[iPotrero][iEstacion]+1));
                }
                writer.println(";");

            }
            writer.println();
            writer.println();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}

