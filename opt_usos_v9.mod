set K;		# usos enteros
set I;		# usos ESTACIONARIO
set J;		# pixeles
set P;		# productores
set D;		# estaciones-año del período de planificación
set E;		# estaciones (O,I,P,V)
set IK{K} within I;		# usos estacionario del uso entero k
set JP{P} within J;		# pixeles del productor p
set PI{I} within I;		# usos estacionario Previo al uso estacionario i

param DURACION>=0 integer;		# duracion de la planificacion en estaciones		
param S{J} >= 0;				# superficie del pixel j
param F{I} >= 0;				# fósforo que exporta el uso estacionario i, ya no depende de la estación e
param G{I} >= 0;				# productividad del uso estacionario i, ya no depende de la estación e
param DUR{K} >= 0 integer;		# duración del uso k en cantidad de estaciones-año
param C{K,D} binary;			# indica si se puede comenzar el uso k en la estación-año d
param T{D};						# traduce de estación-año a estación
param MIN_USOS >= 0 integer;	# mínima cantidad de usos enteros por productor por estación-año
param MAX_USOS >= 0 integer;	# máxima cantidad de usos enteros por productor por estación-año
param MIN_PROD{E} >= 0;			# mínima productividad por productor por estación
param USO_CERO{J, I} binary;	# indica si el usos estacionario I en el pixel J esta presente en el momento previo a la planificacion

var X {I,J,D} binary;	# indica si se asigna el uso estacionario i al pixel j en la estación-año d
var Z {K,J,D} binary;	# indica si se comienza el uso k en el pixel j en la estación-año d
var Y {K,J,D} binary;	# indica si se asigna el uso entero k en el pixel j en la estación-año d
var W {K,P,D} binary; 	# indica si el uso entero k está asignado a algún pixel del productor p en la estación-año d

minimize fosforo_total: sum {i in I} sum{j in J} sum{d in D} X[i,j,d]*S[j]*F[i]; #+ sum {k in K} sum{j in J} sum{d in D} (Y[k,j,d] + Z[k,j,d]); 

subject to comienzo_permitido {k in K, j in J, d in D}: Z[k,j,d] <= C[k,d];

subject to comienzo_uso_entero_pixel {j in J, d in D}: sum {k in K} Z[k,j,d] <= 1;

subject to asig_uso_entero_pixel {j in J, d in D}: sum {k in K} Y[k,j,d] = 1;

subject to asig_uso_anual_pixel {j in J, d in D}: sum {i in I} X[i,j,d] = 1;

subject to min_productividad {p in P, d in D}: sum {i in I, j in JP[p]} X[i,j,d]*S[j]*G[i] >= MIN_PROD[T[d]]* sum {j in JP[p]} S[j];

subject to min_usos_diferentes {p in P, d in D}: sum {k in K} W[k,p,d] >= MIN_USOS;

subject to max_usos_diferentes {p in P, d in D}: sum {k in K} W[k,p,d] <= MAX_USOS;

subject to contar_uso_entero {k in K, p in P, d in D}: W[k,p,d] >= sum {j in JP[p]} Y[k,j,d] / card(JP[p]); # revisar ... vinculo entre W y Y

subject to usos_enteros_anuales {k in K, i in IK[k], j in J, d in D}: Y[k,j,d] >= X[i,j,d];

subject to usos_anuales_comienzos {d in D, k in K, i in IK[k], j in J, dd in d..min(d+DUR[k],DURACION)}: X[i,j,dd] >= Z[k,j,d];

subject to uso_previo {i in I, j in J, d in 1..(DURACION-1)}: X[i,j,d+1] <= sum {ii in PI[i]}  X[ii,j,d];

subject to uso_cero {i in I, j in J}: X[i,j,1] <= sum {ii in PI[i]}  USO_CERO[j, ii];

solve;

# Report / Result Section (Optional)

#para cada pixel
for {j in J} {
 printf '  %.0f:=',j;
 #para cada duracion
 for {d in D} {
	#para cada uso estacionario
 	for {i in I:X[i,j,d]==1} {
 		printf ' %3s',(i);
 		
 	}
 }
 printf ';\n';
}

end;
