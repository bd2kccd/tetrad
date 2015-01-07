///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.data;

import edu.cmu.tetrad.graph.Graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A completely blank knowledge with all default returns for methods. The idea of this
 * is to override only the methods you need with specific operations to allow use of
 * knowledge to be fast.
 *
 * @author Joseph Ramsey
 */
public class BlankKnowledge implements IKnowledge {
    public void addToTier(int tier, String var) {
        // no op.
    }

    public void addToTiersByVarNames(List<String> varNames) {
        // no op./
    }

    public List<KnowledgeGroup> getKnowledgeGroups() {
        return new ArrayList<KnowledgeGroup>();
    }

    public void removeKnowledgeGroup(int index) {
        // no op
    }

    public void addKnowledgeGroup(KnowledgeGroup group) {
        // no op
    }

    public void setKnowledgeGroup(int index, KnowledgeGroup group) {
        // no op.
    }

    public Iterator<KnowledgeEdge> forbiddenCommonCausesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    public Iterator<KnowledgeEdge> forbiddenEdgesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    public Iterator<KnowledgeEdge> explicitlyForbiddenEdgesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    public List<String> getVarsNotInTier() {
        return new ArrayList<String>();
    }

    public List<String> getTier(int tier) {
        return new ArrayList<String>();
    }

    public int getNumTiers() {
        return 0;
    }

    public boolean commonCauseForbidden(String var1, String var2) {
        return false;
    }

    public boolean edgeExplicitlyRequired(String var1, String var2) {
        return false;
    }

    public boolean edgeExplicitlyRequired(KnowledgeEdge edge) {
        return false;
    }

    public boolean edgeForbidden(String var1, String var2) {
        return false;
    }

    public boolean edgeRequired(String var1, String var2) {
        return false;
    }

    public boolean edgeRequiredByGroups(String var1, String var2) {
        return false;
    }

    public boolean edgeForbiddenByGroups(String var1, String var2) {
        return false;
    }

    public boolean noEdgeRequired(String x, String y) {
        return true;
    }

    public boolean isForbiddenByTiers(String var1, String var2) {
        return false;
    }

    public boolean isEmpty() {
        return true;
    }

    public Iterator<KnowledgeEdge> requiredCommonCausesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    public Iterator<KnowledgeEdge> requiredEdgesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    public Iterator<KnowledgeEdge> explicitlyRequiredEdgesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    public void setEdgeForbidden(String var1, String var2, boolean forbid) {
        // no op
    }

    public void setEdgeRequired(String var1, String var2, boolean required) {
        // no op
    }

    public void removeFromTiers(String var) {
        // no op
    }

    public void setTierForbiddenWithin(int tier, boolean forbidden) {
        // no op
    }

    public boolean isTierForbiddenWithin(int tier) {
        return false;
    }

    public int getMaxTierForbiddenWithin() {
        return 0;
    }

    public void setDefaultToKnowledgeLayout(boolean defaultToKnowledgeLayout) {
        // no op
    }

    public boolean isDefaultToKnowledgeLayout() {
        return false;
    }

    public void clear() {
        // no op.
    }

    public boolean isViolatedBy(Graph graph) {
        return false;
    }

    public void setTier(int tier, List<String> vars) {
        // no op
    }

    public void addVariable(String varName) {
        // no op
    }

    public void removeVariable(String varName) {
        // no op
    }

    public List<String> getVariables() {
        return new ArrayList<String>();
    }
}

