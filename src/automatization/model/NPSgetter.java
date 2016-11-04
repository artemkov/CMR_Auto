/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatization.model;

/**
 *
 * @author Артем Ковалев
 */
public interface NPSgetter 
{
    Double getNps();
    Double getTops();
    Double getBottoms();
    Double getPassives();
    Double getGroupedTotal();
    Double getStandartDeviation();
}
