package com.fuelflex.platform.pump.entity;

/**
 * Définit le niveau auquel les index de distribution sont suivis.
 *
 * PUMP :
 * - la pompe représente directement le niveau de comptage ;
 * - un compteur global sera rattaché à la pompe ;
 * - les points de distribution partageront ce relevé.
 *
 * DISPENSING_POINT :
 * - la pompe représente un distributeur physique ;
 * - chaque point de distribution porte son propre niveau de comptage ;
 * - les relevés seront suivis séparément.
 */
public enum MeteringLevel {

    PUMP,

    DISPENSING_POINT
}
