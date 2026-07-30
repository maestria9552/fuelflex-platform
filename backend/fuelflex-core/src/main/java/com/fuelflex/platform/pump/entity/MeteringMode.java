package com.fuelflex.platform.pump.entity;

/**
 * Définit le niveau auquel les index de distribution sont suivis.
 *
 * PUMP_LEVEL :
 * - la pompe représente directement le point de distribution ;
 * - la citerne et l'index courant sont rattachés à la pompe ;
 * - aucun pistolet n'est nécessaire.
 *
 * NOZZLE_LEVEL :
 * - la pompe représente un distributeur physique ;
 * - chaque pistolet possède sa citerne et son index courant ;
 * - les relevés et les ventes sont suivis par pistolet.
 */
public enum MeteringMode {

    PUMP_LEVEL,

    NOZZLE_LEVEL
}