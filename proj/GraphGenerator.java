package proj;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

public class GraphGenerator {
    public CFG createCFG(String className) throws ClassNotFoundException {
        CFG cfg = new CFG();
        JavaClass jc = Repository.lookupClass(className);
        ClassGen cg = new ClassGen(jc);
        ConstantPoolGen cpg = cg.getConstantPool();

        for (Method m: cg.getMethods()) {
            MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
            InstructionList il = mg.getInstructionList();
            InstructionHandle[] handles = il.getInstructionHandles();
            for (InstructionHandle ih: handles) {
                int position = ih.getPosition();
                cfg.addNode(position, m, jc);
                Instruction inst = ih.getInstruction();
                // your code goes here
                if (!(inst instanceof BranchInstruction)){
                    if(inst instanceof ReturnInstruction) {
                        cfg.addEdge(position, m, jc, -1, m, jc);
                    } else{
                        cfg.addEdge(position, m, jc, ih.getNext().getPosition(), m, jc);
                    }
                } else {
                    if(inst instanceof IfInstruction){
                        cfg.addEdge(position, m, jc, ih.getNext().getPosition(), m, jc);
                    }
                    cfg.addEdge(position, m, jc, ((BranchInstruction) inst).getTarget().getPosition(), m, jc);
                }
            }
        }
        return cfg;
    }

    public CFG createCFGWithMethodInvocation(String className) throws ClassNotFoundException {
        // your code goes here
        CFG cfg = new CFG();
        JavaClass jc = Repository.lookupClass(className);
        ClassGen cg = new ClassGen(jc);
        ConstantPoolGen cpg = cg.getConstantPool();

        for (Method m: cg.getMethods()) {
            MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
            InstructionList il = mg.getInstructionList();
            InstructionHandle[] handles = il.getInstructionHandles();
            for (InstructionHandle ih: handles) {
                int position = ih.getPosition();
                cfg.addNode(position, m, jc);
                Instruction inst = ih.getInstruction();
                if (!(inst instanceof BranchInstruction)){
                    if(inst instanceof ReturnInstruction) {
                        cfg.addEdge(position, m, jc, -1, m, jc);
                    } else if(inst instanceof INVOKESTATIC) {
                        for (Method mtd: cg.getMethods()) {
                            if((mtd.getName() == ((INVOKESTATIC) inst).getMethodName(cpg)) &&
                                    (mtd.getSignature() == ((INVOKESTATIC) inst).getSignature(cpg))){
                                MethodGen m_gen = new MethodGen(mtd, cg.getClassName(), cpg);
                                InstructionList i_list = m_gen.getInstructionList();
                                InstructionHandle[] i_handles = i_list.getInstructionHandles();
                                cfg.addEdge(position, m, jc, i_handles[0].getPosition(), mtd, jc);
                                cfg.addEdge(-1, mtd, jc, ih.getNext().getPosition(), m, jc);
                            }
                        }
                    } else{
                        cfg.addEdge(position, m, jc, ih.getNext().getPosition(), m, jc);
                    }
                } else {
                    if(inst instanceof IfInstruction){
                        cfg.addEdge(position, m, jc, ih.getNext().getPosition(), m, jc);
                    }
                    cfg.addEdge(position, m, jc, ((BranchInstruction) inst).getTarget().getPosition(), m, jc);
                }
            }
        }
        return cfg;
    }

    public static void main(String[] a) throws ClassNotFoundException {
        GraphGenerator gg = new GraphGenerator();
//        CFG res_mtd = gg.createCFGWithMethodInvocation("proj.D"); // example invocation of createCFGWithMethodInovcation
//        System.out.println(res_mtd.toString());
    }
}
