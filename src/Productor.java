import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.min;

public class Productor {
    int numeroProductor;
    float[] restriccionProduccionEstacion;
    List<Integer> pixelesDelProductor;
    float areaTotal;

    /**Devuelve el minimo entre la minima cantidad de usos y la cantidad de pixeles del productor**/
    public int getMinCantUsos(){
        return min(this.pixelesDelProductor.size(), Constantes.minimaCantidadUsos);
    }

    /**Constructor de Productor**/
    //TODO: quitar restriccionProduccionAnual
    public Productor(int numeroProductor, float[] restriccionProduccionEstacion, float[] restriccionProduccionAnual, List<Integer> pixelesDelProductor) {
        this.numeroProductor = numeroProductor;
        this.restriccionProduccionEstacion = restriccionProduccionEstacion;
        this.pixelesDelProductor = pixelesDelProductor;
        this.areaTotal=0;
    }

    /**Carta tantos productores como COnstantes.cantProductores**/
    public static Productor[] cargarProductores(){
        float[] restriccionProductorE, restriccionProductorA;
        List<Integer> pixelesDelProductor;
        Productor[] productores = new Productor[Constantes.cantProductores];
        //El productor cero tiene todos los pixeles pares
        restriccionProductorE= new float[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        restriccionProductorA= new float[] {0,0,0,0};
        pixelesDelProductor= new ArrayList<Integer>();


        //PRUEBA SIN RESTRICCIONES PARA EL PRODUCTOR 1 CON INDICE 0
        productores[0]= new Productor(0,restriccionProductorE, restriccionProductorA,pixelesDelProductor );
        for (int iProductores = 1; iProductores < Constantes.cantProductores; iProductores++) {
            //El productor cero tiene todos los pixeles pares

            restriccionProductorA= new float[] {6000,6000,6000,6000};
            pixelesDelProductor= new ArrayList<Integer>();
            productores[iProductores]= new Productor(iProductores,Constantes.restriccionProductividadProductorE, restriccionProductorA,pixelesDelProductor );
        }

        return productores;
    }

    /**Carga dos Productores test**/
    public static Productor[] cargarProductoresTest(){
        Productor[] productores = new Productor[Constantes.cantProductores];

        //El productor cero tiene todos los pixeles pares
        float[] restriccionProductorE0= new float[] {1200,600,2100,2100,1200,600,2100,2100,1200,600,2100,2100};
        float[] restriccionProductorA0= new float[] {6000,6000,6000};
        productores[0]= new Productor(0,restriccionProductorE0, restriccionProductorA0,Arrays.asList(2,4));
        //El productor uno tiene todos los pixeles inpares
        float[] restriccionProductorE1= new float[] {1200,600,2100,2100,1200,600,2100,2100,1200,600,2100,2100};
        float[] restriccionProductorA1= new float[] {6000,6000,6000};
        productores[1]= new Productor(0,restriccionProductorE1, restriccionProductorA1,Arrays.asList(1,3));
        return productores;
    }

    /**Imprime los atributos de un Productor**/
    public void imprimirProductor(){
        System.out.printf("("+this.numeroProductor+", "+this.areaTotal+", {");

        for (int i = 0; i <this.restriccionProduccionEstacion.length ; i++) {
            System.out.print(this.restriccionProduccionEstacion[i]);
            if (i!=(this.restriccionProduccionEstacion.length-1)){
                System.out.printf(",");
            }
        }
        System.out.printf("}, {");
        for (int i = 0; i <this.pixelesDelProductor.size(); i++) {
            System.out.print(this.pixelesDelProductor.get(i));
            if (i!=(this.pixelesDelProductor.size()-1)) {
                System.out.printf(",");
            }
        }
        System.out.println("})");
    }

    /**Imprime todos los productores**/
    public static void imprimirProductores(){
        for (int i = 0; i < Constantes.productores.length; i++) {
            Constantes.productores[i].imprimirProductor();
        }
    }

    /**Imprime los productores que han participado del problema**/
    public static void imprimirProductoresActivos(){
        for (int i = 0; i < Constantes.productoresActivos.size(); i++) {
            Constantes.productores[Constantes.productoresActivos.get(i)].imprimirProductor();
        }
    }

}
