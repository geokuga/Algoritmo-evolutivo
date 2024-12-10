import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class ClaseAction extends Agent {
    int poblacion = 500;
    int generaciones = 500;
    double tasaCruce = 0.9;
    double tasaMutacion = 0.05;

    double inicio = 8;
    double fin = 12;
    double[] coeficientes;

    @Override
    protected void setup() {
        addBehaviour(new Genetico());
        addBehaviour(new Predicciones());
    }

    protected class Genetico extends OneShotBehaviour {
        @Override
        public void action() {
            double[] X = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            double[] Y = {3, 6, 9, 12, 15, 18, 21, 24, 27};

            ArrayList<double[]> poblacion = new ArrayList<>();
            Random random = new Random();

            // Inicialización de la población
            for (int i = 0; i < ClaseAction.this.poblacion; i++) {
                double beta0 = random.nextDouble() * 200 - 100; // Valores entre -100 y 100
                double beta1 = random.nextDouble() * 200 - 100;
                poblacion.add(new double[]{beta0, beta1, 0}); // [β0, β1, aptitud]
            }

            // Evaluar la población inicial
            for (double[] individuo : poblacion) {
                individuo[2] = calcularAptitud(individuo[0], individuo[1], X, Y);
            }

            for (int generacion = 1; generacion <= generaciones; generacion++) {
                System.out.println("Generación " + generacion);

                ArrayList<double[]> nuevaPoblacion = new ArrayList<>();

                // Elitismo: mantener al mejor individuo
                double[] mejor = poblacion.stream().max((a, b) -> Double.compare(a[2], b[2])).orElse(null);
                nuevaPoblacion.add(mejor);

                // Cruce y mutación
                while (nuevaPoblacion.size() < ClaseAction.this.poblacion) {
                    // Selección de padres
                    double sumaAptitud = poblacion.stream().mapToDouble(ind -> ind[2]).sum();
                    double[] padre1 = seleccionarPadre(poblacion, sumaAptitud);
                    double[] padre2 = seleccionarPadre(poblacion, sumaAptitud);

                    if (Math.random() < tasaCruce) {
                        // Cruce
                        double beta0Hijo = random.nextBoolean() ? padre1[0] : padre2[0];
                        double beta1Hijo = random.nextBoolean() ? padre1[1] : padre2[1];
                        nuevaPoblacion.add(new double[]{beta0Hijo, beta1Hijo, 0});
                    } else {
                        // Copiar un padre
                        nuevaPoblacion.add(new double[]{padre1[0], padre1[1], 0});
                    }
                }

                // Mutación
                for (double[] individuo : nuevaPoblacion) {
                    if (Math.random() < tasaMutacion) {
                        if (random.nextBoolean()) {
                            individuo[0] += random.nextGaussian();
                        } else {
                            individuo[1] += random.nextGaussian();
                        }
                    }
                }

                // Evaluar la nueva población
                for (double[] individuo : nuevaPoblacion) {
                    individuo[2] = calcularAptitud(individuo[0], individuo[1], X, Y);
                }

                poblacion = nuevaPoblacion;
            }

            coeficientes = poblacion.stream().max((a, b) -> Double.compare(a[2], b[2])).orElse(null);
            System.out.println();
            System.out.println("  R²: " + coeficientes[2]);
            System.out.println();
            System.out.println("  β0: " + coeficientes[0]);
            System.out.println("  β1: " + coeficientes[1]);
            System.out.println();
            System.out.println("    (X)    (Y)");
        }
    }





    
    protected class Predicciones extends CyclicBehaviour{
        DecimalFormat formato = new DecimalFormat("#.##");

        @Override
        public void action() {
            if (inicio > fin) {
                myAgent.removeBehaviour(this);
                return;
            }
             double pronostico = coeficientes[0] + coeficientes[1] * inicio;
            System.out.print("    " + inicio + "    " + formato.format(pronostico));
            System.out.println("        ŷ = " + coeficientes[0] + " + " + coeficientes[1] + " ( " + inicio + " ) ");
            inicio++;
        }
    }





    protected double calcularAptitud(double beta0, double beta1, double[] X, double[] Y) {
        double sumaY = 0;
        for (double y : Y) {
            sumaY += y;
        }
        double mediaY = sumaY / Y.length;

        double ssTotal = 0, ssResidual = 0;
        for (int i = 0; i < X.length; i++) {
            double prediccion = beta0 + beta1 * X[i];
            ssTotal += Math.pow(Y[i] - mediaY, 2);
            ssResidual += Math.pow(Y[i] - prediccion, 2);
        }

        return ssTotal == 0 ? 0 : 1 - (ssResidual / ssTotal);
    }




    protected double[] seleccionarPadre(ArrayList<double[]> poblacion, double sumaAptitud) {
        double umbral = Math.random() * sumaAptitud;
        double suma = 0;

        for (double[] ind : poblacion) {
            suma += ind[2];
            if (suma >= umbral) {
                return ind;
            }
        }
        return poblacion.get(poblacion.size() - 1);
    }
}
