package org.rcsb.sequence.biojavadao;


import static org.rcsb.sequence.model.ResidueNumberScheme.ATOM;

import java.util.Map;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.AminoAcidImpl;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.secstruc.SecStrucInfo;
import org.biojava.nbio.structure.secstruc.SecStrucType;
import org.rcsb.sequence.annotations.SecondaryStructureType;
import org.rcsb.sequence.annotations.SecondaryStructureValue;
import org.rcsb.sequence.conf.AnnotationClassification;
import org.rcsb.sequence.conf.AnnotationName;
import org.rcsb.sequence.conf.AnnotationRegistry;
import org.rcsb.sequence.core.AbstractAnnotationGroup;
import org.rcsb.sequence.model.ResidueId;
import org.rcsb.sequence.model.ResidueNumberScheme;
import org.rcsb.sequence.model.Sequence;
import org.rcsb.sequence.util.AnnotationConstants;

public class BioJavaSecStrucAnnotationGroup
        extends AbstractAnnotationGroup<String> {

    public static final String annotationName = AnnotationConstants.authorSecStruc;
    /**
     *
     */
    private static final long serialVersionUID = -1698453992883973704L;
    BioJavaChainProxy proxy;

    public BioJavaSecStrucAnnotationGroup(Sequence chain) {
        super(AnnotationClassification.secstr, AnnotationRegistry.getAnnotationByName(annotationName), ResidueNumberScheme.SEQRES, chain);
    }

    public BioJavaSecStrucAnnotationGroup(BioJavaChainProxy chain, AnnotationClassification ac, AnnotationName name) {

        super(ac, name, ResidueNumberScheme.SEQRES, chain);
        this.proxy = chain;
    }

    @Override
    protected void constructAnnotationsImpl() throws Exception {

        SecStrucType prevSecStr = null;
        int prevStart = -1;
        int prevEnd = -1;
        int currPos = -1;
        for (Group g : proxy.getBJChain().getSeqResGroups()) {

//			currPos++;


            if (g.getType().equals(AminoAcidImpl.type)) {
                AminoAcid aa = (AminoAcid) g;

                SecStrucType s = SecStrucType.coil;

                if (!aa.getAtoms().isEmpty()) {
                    currPos = g.getResidueNumber().getSeqNum();

                    if ( g.getProperty(Group.SEC_STRUC)  != null ){

                        SecStrucInfo ss = (SecStrucInfo) g.getProperty(Group.SEC_STRUC);

                        s = ss.getType();
                    }
                }

                if (prevSecStr == null) {
                    prevSecStr = s;
                    prevStart = currPos;
                    prevEnd = currPos;
                    continue;
                }

                if (!s.equals(prevSecStr)) {
                    // we found the beginning of a new element.
                    addElement(prevStart, prevEnd, prevSecStr);
                    prevSecStr = s;
                    prevStart = currPos;
                    prevEnd = currPos;
                } else {
                    prevEnd = currPos;
                }
            }
        }
        // add the last element:
        addElement(prevStart, prevEnd, prevSecStr);
    }

    private void addElement(int prevStart, int prevEnd, SecStrucType prevSecStr) {

        //System.out.println("adding sec struct" + prevStart + " " + prevEnd + " " + prevSecStr);

        ResidueId start = getResidueId(prevStart);
        ResidueId end = getResidueId(prevEnd);
        if (prevSecStr.equals(" "))
            addAnnotation(new SecondaryStructureValue(SecondaryStructureType.empty), start, end);
        else {
            if (prevSecStr.equals(SecStrucType.extended)) {
                addAnnotation(new SecondaryStructureValue(SecondaryStructureType.E), start, end);
            } else if (prevSecStr.equals(SecStrucType.helix4)) {
                addAnnotation(new SecondaryStructureValue(SecondaryStructureType.H), start, end);
            }
        }
        //System.out.println(" now got " + annotations.size()  + " annotations " + getAnnotationCount() + " " + getAnnotationValueCount());
    }

    private ResidueId getResidueId(Integer id) {
        return chain.getResidueId(ATOM, id);
//		ResidueId result = chain.getResidueId(SEQRES, id);
//		if(result != null) result = result.getEquivalentResidueId(ATOM);
//		if(result == null) System.err.println("Can't find mmcif residue " + id + " on chain " + chain);
//		return result;
    }
}