//Para lectura de archivo



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Pixel {
    int numero, productor, potrero, id;
    float[] cordenada = new float [2];
    float superficie, distanciaAlRio;
    String usoOriginal;

    //TODO: Seguir con comentarios desde aca

    //Esto no se va en la solucion
    // int[] decisiones = new int[Constantes.cantEstaciones]; //Se guarda el Uso*100+el numero de estacion de ese uso
    /**Constructor de la clase Pixel**/
    public Pixel(int numero, int id, int productor, int potrero, float[] cordenada, float superficie, float distanciaAlRio, String usoOriginal) {
        this.numero = numero;
        this.id = id;
        this.productor = productor;
        this.potrero = potrero;
        this.cordenada = cordenada;
        this.superficie = superficie;
        this.distanciaAlRio = distanciaAlRio;
        this.usoOriginal = usoOriginal;
    }

    /**Imprime en pantalla todos los datos del pixel**/
    public void imprimirPixel(){
            System.out.printf("\t("+this.numero+","+this.id+ ","+this.productor+","+this.potrero+
                    ", {"+this.cordenada[0]+","+this.cordenada[1]+"},"+
                    this.superficie+","+this.distanciaAlRio+","+this.usoOriginal+")%n");
    }

    /**Imprime todos los pixeles cargados en la lista Constantes.pixeles**/
    public static void imprimirPixeles(){
        System.out.println("\t(numero, id, productor, potrero, {coordenadas}, superficie, distanciaAlRio, usoOriginal)");
        System.out.println("For: "+Constantes.pixeles.length);
        for (int i = 0; i < Constantes.pixeles.length; i++) {
            System.out.print("i=" +i+" ");
            Constantes.pixeles[i].imprimirPixel();
        }
        //System.out.println("Salgo del for");
    }

    /**Deprecated: carga pixeles de testeo**/
    public static Pixel[] cargarPixelesTest(){
        Pixel[] pixeles=new Pixel[Constantes.cantPixeles];
        float[] cordenadaPixel;
        for (int iPixel = 0; iPixel < Constantes.cantPixeles; iPixel++) {
            cordenadaPixel=new float[]{iPixel,iPixel};
            pixeles[iPixel]=new Pixel(iPixel,iPixel,iPixel%2,iPixel, cordenadaPixel,iPixel,iPixel,"Prueba");
        }
        return pixeles;
    }

    //TODO: Exepcion No exite el uso.
    //TODO: Exepcion Excede la duracion.
    /**Martin?**/
    public static Pixel[] cargarPixeles(String fileName){
        //Se abre el archivo .dbf se pasa a un texto plano, se remplazan las ',' por ',' para que no haya problemas al pasarlo de strings a float
        //Se borran los usos que no queremos con reg exp: ^.*<Uso>.*$\n para los usos: Bajo, Buffer, Calle, Forestacion, Humedal, Monte Nativo, Tajamar,Tambo
        Pixel[] pixeles=new Pixel[Constantes.cantPixeles];
        Constantes.productoresActivos = new ArrayList<Integer>();
        int  iPixel=0, id, productor, potrero;
        float superficie, distanciaAlRio;
        float[] cordenadaPixel;
        String usoOriginal;
        // The name of the file to open.

        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            //Leo linea con el encabezado:
            //PROD	POT	ID	SUP_HA	USO
            line = bufferedReader.readLine();

            //Leo el resto de las lineas
            while((line = bufferedReader.readLine()) != null) {
                //System.out.println("Linea "+iPixel+":"+line);
                //Parceo la linea segun sus separadores
                String[] campos = line.split("\\t");
                if (campos.length>0) {
                    //Filtro las calles
                    if (!campos[0].equalsIgnoreCase("Calle")&& campos.length>0) {
                        //Filtro los usos que anulan los pixeles
                        if (!campos[4].equalsIgnoreCase("Bajo")&&
                                !campos[4].equalsIgnoreCase("Buffer") &&
                                !campos[4].equalsIgnoreCase("Calle") &&
                                !campos[4].equalsIgnoreCase("Forestacion") &&
                                !campos[4].equalsIgnoreCase("Humedal")&&
                                !campos[4].equalsIgnoreCase("Monte Nativo")&&
                                !campos[4].equalsIgnoreCase("Tajamar")&&
                                !campos[4].equalsIgnoreCase("Tambo")) {

                            id = Integer.valueOf(campos[2]);
                            productor = Integer.valueOf(campos[0])-1;
                            if (!Constantes.productores[productor].pixelesDelProductor.contains(iPixel)){
                                //System.out.println("Cargo el pixel "+iPixel+" en el productor "+(productor));
                                Constantes.productores[productor].pixelesDelProductor.add(iPixel);
                            }
                            if (!Constantes.productoresActivos.contains(productor)){
                                Constantes.productoresActivos.add(productor);
                                //System.out.println("Activo el productor: "+productor);
                            }

                            potrero = Integer.valueOf(campos[1]);
                            superficie = Float.valueOf(campos[3]);
                            Constantes.productores[productor].areaTotal+=superficie;
                            usoOriginal = campos[4];

                            //Siguiente version
                            cordenadaPixel = new float[]{0F, 0F};
                            distanciaAlRio = 0;//Float.valueOf(campos[13]);
                            //Creo un Pixel con sus datos
                            Pixel pixelNuevo = new Pixel(iPixel, id, productor, potrero, cordenadaPixel, superficie, distanciaAlRio, usoOriginal);
                            //pixelNuevo.imprimirPixel();
                            //Lo agrego al arreglo de pixeles
                            pixeles[iPixel] = pixelNuevo;

                            iPixel++;
                        }
                    }
                }
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "CargarPixel: Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        return pixeles;
    }
    /**Martin?**/
    //TODO: Exepcion No exite el uso.
    //TODO: Exepcion Excede la duracion.
    public static Pixel[] cargarPixelesDeProductor(String fileName, int numProductor){
        //Se abre el archivo .dbf se pasa a un texto plano, se remplazan las ',' por ',' para que no haya problemas al pasarlo de strings a float
        //Se borran los usos que no queremos con reg exp: ^.*<Uso>.*$\n para los usos: Bajo, Buffer, Calle, Forestacion, Humedal, Monte Nativo, Tajamar,Tambo

        //Calculo cuantos pixeles hay par ael productor



        int  iPixel=0, id, productor, potrero,  cantPixeles=0;
        float superficie, distanciaAlRio;
        float[] cordenadaPixel;
        String usoOriginal;
        // The name of the file to open.

        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            //Leo linea con el encabezado:
            //PROD	POT	ID	SUP_HA	USO
            line = bufferedReader.readLine();

            //Leo el resto de las lineas
            while((line = bufferedReader.readLine()) != null) {
                //System.out.println("Linea "+iPixel+":"+line);
                //Parceo la linea segun sus separadores
                String[] campos = line.split("\\t");
                if (campos.length>0) {
                    //Filtro las calles
                    //System.out.println("Comparo: "+ Integer.valueOf(campos[0]) +"\tcon "+numProductor);
                    if (Integer.valueOf(campos[0])== numProductor) {
                       cantPixeles++;
                       //System.out.println("ACA");
                    }
                }
            }
            Constantes.cantPixeles=cantPixeles;

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "CargarPixel: Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        System.out.println("Productor: "+numProductor+"\tCantPixeles: "+cantPixeles);

        Pixel[] pixeles=new Pixel[cantPixeles];
        Constantes.productoresActivos = new ArrayList<Integer>();


        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            //Leo linea con el encabezado:
            //PROD	POT	ID	SUP_HA	USO
            line = bufferedReader.readLine();

            //Leo el resto de las lineas
            while((line = bufferedReader.readLine()) != null) {
                //System.out.println("Linea "+iPixel+":"+line);
                //Parceo la linea segun sus separadores
                String[] campos = line.split("\\t");
                if (campos.length>0) {
                    //Filtro las calles
                    if (Integer.valueOf(campos[0])== numProductor) {
                        //Filtro los usos que anulan los pixeles
                        if (!campos[4].equalsIgnoreCase("Bajo")&&
                                !campos[4].equalsIgnoreCase("Buffer") &&
                                !campos[4].equalsIgnoreCase("Calle") &&
                                !campos[4].equalsIgnoreCase("Forestacion") &&
                                !campos[4].equalsIgnoreCase("Humedal")&&
                                !campos[4].equalsIgnoreCase("Monte Nativo")&&
                                !campos[4].equalsIgnoreCase("Tajamar")&&
                                !campos[4].equalsIgnoreCase("Tambo")) {

                            id = Integer.valueOf(campos[2]);
                            productor = Integer.valueOf(campos[0])-1;
                            if (!Constantes.productores[productor].pixelesDelProductor.contains(iPixel)){
                                //System.out.println("Cargo el pixel "+iPixel+" en el productor "+(productor));
                                Constantes.productores[productor].pixelesDelProductor.add(iPixel);
                            }
                            if (!Constantes.productoresActivos.contains(productor)){
                                Constantes.productoresActivos.add(productor);
                                //System.out.println("Activo el productor: "+productor);
                            }

                            potrero = Integer.valueOf(campos[1]);
                            superficie = Float.valueOf(campos[3]);
                            Constantes.productores[productor].areaTotal+=superficie;
                            usoOriginal = campos[4];

                            //Siguiente version
                            cordenadaPixel = new float[]{0F, 0F};
                            distanciaAlRio = 0;//Float.valueOf(campos[13]);
                            //Creo un Pixel con sus datos
                            Pixel pixelNuevo = new Pixel(iPixel, id, productor, potrero, cordenadaPixel, superficie, distanciaAlRio, usoOriginal);
                            //pixelNuevo.imprimirPixel();
                            //Lo agrego al arreglo de pixeles
                            pixeles[iPixel] = pixelNuevo;

                            iPixel++;
                        }
                    }
                }
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "CargarPixel: Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        return pixeles;
    }
    /**Cuenta las lineas de un archivo.**/
    public static int contarLineas(String fileName){
        int  cantLineas=0;
        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            //Leo linea con el encabezado:
            //PROD	POT	ID	SUP_HA	USO
            line = bufferedReader.readLine();

            //Leo el resto de las lineas
            while((line = bufferedReader.readLine()) != null) {
                cantLineas++;
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        return cantLineas;
    }
}