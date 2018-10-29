
import java.util.*;
import java.io.*;



public class FinalImplementation {

    
    public static void main(String[] args) {
        
        //reading from file
        HeaderTable headerTable = new HeaderTable();
        headerTable.readData("processed transactions.txt");
        
        //tree construction
        double clockTime = System.currentTimeMillis();
        CpTree root = new CpTree();
        root.transactionInsert(root, headerTable);
        double res = System.currentTimeMillis()-clockTime;
        System.out.println("total node count: "+root.totalNodeCounter(root));
        System.out.println("total time to make tree: "+res);
        //debug: root.printCpTree(root);
        int totalTransaction=0;
        for(int i=0;i<headerTable.transactions.size();i++){
            totalTransaction += headerTable.transactions.get(i).size();
        }
        System.out.println("Give Minimum Support Threshold in percentage");
        Scanner scan = new Scanner(System.in);
        double v = scan.nextDouble();
                
        double value=v;
        double minimumSupportThreshold = Math.ceil(value/100.0*totalTransaction);
        System.out.println("minimum support threshold in count "+minimumSupportThreshold);
        //mine pattern
        clockTime = System.currentTimeMillis();
        PatternMining patternTree = new PatternMining();
        patternTree.beginMining(root, patternTree, headerTable,minimumSupportThreshold);
        res = System.currentTimeMillis()-clockTime;
        System.out.println("Total time to generate patterns: "+res);
    }
    
}

//CP Tree class 
class CpTree {
    int Max=255;
    CpTree child[];  //child links
    CpTree parent; //parent node
    int characterFromParent; //character through which it came from parent
    ArrayList<Integer>batchFrequency; //batch frequency saver
    CpTree tempNode; //temporary node to capture links
    int tempCharacter; //temporary character to temporary nodes
    
    //DEBUG
    boolean debug;
    
    public CpTree(){
        child = new CpTree[Max+2];
        batchFrequency = new ArrayList<Integer>();
        for(int i=0;i<Max;i++){
            child[i]=null; 
        }
        tempNode=null;
        
    }
    
    void transactionInsert(CpTree root,HeaderTable headerTable){
        //element of item controller class
        ItemControllerClass items = new ItemControllerClass();
        for(int i=0;i<headerTable.transactions.size();i++){
            ArrayList<String>transactions = new ArrayList<String>();
            for(int j=0;j<headerTable.transactions.get(i).size();j++){
                transactions.add(headerTable.transactions.get(i).get(j));
            }
            if(i==0){
                for(int j=0;j<transactions.size();j++){
                    String v = transactions.get(j);
                    v = stringSort(v);
                    transactions.set(j, v);
                }
                for(int j=0;j<transactions.size();j++){
                    String v = transactions.get(j);
                    for(int k=0;k<v.length();k++){
                        int int_val = v.charAt(k);
                        if(items.inputCharacters[int_val] == 0) {
                            items.inputCharacters[int_val]=i+1;
                        }
                    }
                }
                spaceAllocationForCharacter(items,i+1);
                for(int j=0;j<transactions.size();j++){
                    insertIntoCpTree(root,i+1,transactions.get(j),0,items);
                }
                reconstructionBegin(root,items);
                deleteRedundantLinkAddress(items);
            }
            else{
               for(int j=0;j<transactions.size();j++){
                   String v = transactions.get(j);
                   for(int k=0;k<v.length();k++){
                       int int_val = v.charAt(k);
                       if(items.inputCharacters[int_val] == 0){
                           items.inputCharacters[int_val]=i+1;
                       }
                   }
               }
               //space allocation
               spaceAllocationForCharacter(items,i+1);
               //sorting
               rankSelectionForCharacters(items);
               for(int j=0;j<transactions.size();j++){
                   String v = transactions.get(j);
                   v=sortStringWithRank(v,items);
                   insertIntoCpTree(root,i+1,v,0,items);
               }
               reconstructionBegin(root,items);
               deleteRedundantLinkAddress(items);
            }
        }
         //debug: items.printEachItem();
         //debug: printCpTree(root);
    }
    
    void insertIntoCpTree(CpTree tree,int currentBatchNumber,String transaction,int index,ItemControllerClass items){
        if(index >= transaction.length()){
            // all the chracters already inserted
            return;
        }
        int int_val = transaction.charAt(index);
        if(tree.child[int_val] != null){
            //already child exists 
            while(tree.child[int_val].batchFrequency.size()<currentBatchNumber){
                tree.child[int_val].batchFrequency.add(0);
            }
            int v1=tree.child[int_val].batchFrequency.get(currentBatchNumber-1)+1;
            tree.child[int_val].batchFrequency.set(currentBatchNumber-1,v1);
            //modification into item table 
            items.updateIntoPerItem(int_val, currentBatchNumber);
            insertIntoCpTree(tree.child[int_val],currentBatchNumber,transaction,index+1,items);
        }else{
            //node doesn't exist
            tree.child[int_val] = new CpTree();
            tree.child[int_val].parent = tree;
            tree.child[int_val].characterFromParent=int_val;
            while(tree.child[int_val].batchFrequency.size()<currentBatchNumber){
                tree.child[int_val].batchFrequency.add(0);
            }
            int v = tree.child[int_val].batchFrequency.get(currentBatchNumber-1)+1;
            tree.child[int_val].batchFrequency.set(currentBatchNumber-1,v);
            //link establish
            items.addNodeIntoLink(int_val, tree.child[int_val], currentBatchNumber);
            //frequency update
            items.updateIntoPerItem(int_val, currentBatchNumber);
            insertIntoCpTree(tree.child[int_val],currentBatchNumber,transaction,index+1,items);
        }
        return;
    }
    
    //initiation of reconstruction
    void reconstructionBegin(CpTree tree, ItemControllerClass items){
       
        int sz=items.list.size();
        while(true){
            boolean possible=false;
            int start=0;
            int next;
            int work=0;
            while(true){
                next=(start+1)%sz;
                if(items.list.get(start).totalAppeared<items.list.get(next).totalAppeared){
                    int character = items.list.get(next).character;
                    for(int k=0;k<items.list.get(start).link_adress.size();k++){
                        if(items.list.get(start).link_adress.get(k).parent != null){
                          /*if(debug==true && items.list.get(start).character=='a'){
                              System.out.println("tree**********");
                              printCpTree(tree);
                              System.out.println("top "+(char)items.list.get(start).character+" "+(char)items.list.get(next).character);
                               System.out.println("tree**********");
                          }*/
                          reconstruction(items.list.get(start).link_adress.get(k),items,character);
                          /*if(debug==true && items.list.get(start).character=='a'){
                               System.out.println("tree**********");
                                printCpTree(tree);
                               System.out.println("tree**********");
                          }*/
                        }
                    }
                    work++;
                    PerItemInformation obj1=items.list.get(start);
                    PerItemInformation obj2=items.list.get(next);
                    items.list.set(start,obj2);
                    items.list.set(next,obj1);
                    possible=true;
                    break;
                }
                else if(items.list.get(start).totalAppeared == items.list.get(next).totalAppeared){
                    if(items.list.get(start).character>items.list.get(next).character){
                       int character = items.list.get(next).character;
                       /*if(debug==true){
                             printCpTree(tree);
                             System.out.println("top "+(char)items.list.get(start).character+" "+(char)items.list.get(next).character);
                        }*/
                        for(int k=0;k<items.list.get(start).link_adress.size();k++){
                            if(items.list.get(start).link_adress.get(k).parent != null){
                                reconstruction(items.list.get(start).link_adress.get(k),items,character);
                            }
                        }
                        /*if(debug==true){
                              printCpTree(tree);
                        }*/
                        work++;
                        PerItemInformation obj1=items.list.get(start);
                        PerItemInformation obj2=items.list.get(next);
                        items.list.set(start,obj2);
                        items.list.set(next,obj1);
                        possible=true;
                        break;
                    }
                }
                start=(start+1)%sz;
                if(start==(sz-1)){
                   break;
                }
            }
            if(possible == false)break;
        }
    }
    //peforming reconstruction
    void reconstruction(CpTree node,ItemControllerClass items,int childCharacter){
        CpTree x,y,z;
        x=null;
        if(node.parent == null) {
            return;
        } //no parent exist, not a valid node
        x=node.parent;
        y=node;
        if(node.child[childCharacter] == null){
            return;
        } //no child exist for this childCharacter
        z=node.child[childCharacter];
        int v1=returnTotalFrequency(y);
        int v2=returnTotalFrequency(z);
        CpTree yPrime;
        boolean condition=false;
        if(v1>v2){
            condition=true;
            //needs to create another node
            yPrime = new CpTree();
            yPrime.parent=x;
            yPrime.characterFromParent=y.characterFromParent;
            int maximum = Math.max(y.batchFrequency.size(), z.batchFrequency.size());
            while(y.batchFrequency.size()<maximum){
                y.batchFrequency.add(0);
            }
            while(z.batchFrequency.size()<maximum){
                z.batchFrequency.add(0);
            }
            while(yPrime.batchFrequency.size()<maximum){
                yPrime.batchFrequency.add(0);
            }
            //update into batchFrequency
            for(int i=0;i<y.batchFrequency.size();i++){
                int c1=y.batchFrequency.get(i);
                int c2=z.batchFrequency.get(i);
                int diff=c1-c2;
                yPrime.batchFrequency.set(i,diff); 
            }
            //update child
            for(int i=0;i<Max;i++){
                if(y.child[i] != null && i != childCharacter){
                    yPrime.child[i]=y.child[i];
                    y.child[i].parent=yPrime;
                    y.child[i]=null;
                }
            }
            x.child[y.characterFromParent]=yPrime; 
            //link added to the node
            items.addNodeIntoLink(yPrime.characterFromParent, yPrime, -1);
        }
        //child update of y,z
        for(int i=0;i<Max;i++){
            y.child[i]=z.child[i];
            if(y.child[i] != null){
               y.child[i].parent=y;   
            }
            z.child[i] = null; 
        }
        z.child[y.characterFromParent]=y; 
        
        //parent
        z.parent=y.parent;
        y.parent=z;
        //batchFrquency
        int maximum = Math.max(y.batchFrequency.size(), z.batchFrequency.size());
        while(y.batchFrequency.size()<maximum){
            y.batchFrequency.add(0);
         }
        while(z.batchFrequency.size()<maximum){
            z.batchFrequency.add(0);
        }
        for(int i=0;i<y.batchFrequency.size();i++){
            int v=z.batchFrequency.get(i);
            y.batchFrequency.set(i,v);
        }
        if(condition == false) {
            x.child[y.characterFromParent]=null;
        }
        //link with parent
        if(x.child[z.characterFromParent] == null){
            x.child[z.characterFromParent]=z;
            int c1=0,c2=0,c3=0;
            for(int i=0;i<Max;i++){
                if(x.child[i] != null) c1++;
                if(y.child[i] != null) c2++;
                if(z.child[i] != null) c3++;
            }
            return;
        }
        else{
            //need to merge two paths
            reconstructionMergeTwoPath(x,z);
        }
       
    }
    
    void reconstructionMergeTwoPath(CpTree basePath,CpTree mergePath){
        if(basePath.child[mergePath.characterFromParent] == null){
            basePath.child[mergePath.characterFromParent]=mergePath;
            mergePath.parent=basePath;
            return;
        }
        int maximum = Math.max(basePath.child[mergePath.characterFromParent].batchFrequency.size(),mergePath.batchFrequency.size());
        while(basePath.child[mergePath.characterFromParent].batchFrequency.size()<maximum){
            basePath.child[mergePath.characterFromParent].batchFrequency.add(0);
        }
        while(mergePath.batchFrequency.size()<maximum){
            mergePath.batchFrequency.add(0);
        }
        for(int i=0;i<mergePath.batchFrequency.size();i++){
            int v1 = mergePath.batchFrequency.get(i);
            int v2 = basePath.child[mergePath.characterFromParent].batchFrequency.get(i);
            int v=v1+v2;
            basePath.child[mergePath.characterFromParent].batchFrequency.set(i,v);
        }
        mergePath.parent=null; //this node is technically vanished
        //for its child to merge
        for(int i=0;i<Max;i++){
            if(mergePath.child[i] != null){
                reconstructionMergeTwoPath(basePath.child[mergePath.characterFromParent],mergePath.child[i]);
            }
        }
        return;
    }
    
    int returnTotalFrequency(CpTree node){
        int sum=0;
        for(int i=0;i<node.batchFrequency.size();i++){
            sum=sum+node.batchFrequency.get(i);
        }
        return sum;
    }
    
    //function to delete the redundent link address from link address of PerItemInformation
    void deleteRedundantLinkAddress(ItemControllerClass items){
        for(int i=0;i<items.list.size();i++){
            //debug: System.out.println("previous size = "+items.list.get(i).link_adress.size());
            items.list.get(i).deleteRedundantLinkAddress(items.list.get(i));
            //debug: System.out.println("present size = "+items.list.get(i).link_adress.size());
        }
        return;
    }
    
    String stringSort(String s){
        String temp="";
        ArrayList<Character>list = new ArrayList<Character>();
        for(int i=0;i<s.length();i++){
            list.add(s.charAt(i));
        }
        Collections.sort(list);
        for(int i=0;i<list.size();i++){
            temp=temp+list.get(i);
        }
        return temp;
    }
    
    void spaceAllocationForCharacter(ItemControllerClass items,int currentBatch){
        for(int i=0;i<Max;i++){
            if(items.inputCharacters[i] == currentBatch){
                //this node needs to be created
                PerItemInformation object = new PerItemInformation(i);
                items.list.add(object);
            }
        }
    }
    void rankSelectionForCharacters(ItemControllerClass items){
        for(int i=0;i<items.list.size();i++){
            items.characterRank[items.list.get(i).character]=i;
        }
        return;
    }
    String sortStringWithRank(String s,ItemControllerClass items){
        ArrayList<Character>list = new ArrayList<Character>();
        for(int i=0;i<s.length();i++){
            list.add(s.charAt(i));
        }
        for(int i=0;i<list.size();i++){
            for(int j=i+1;j<list.size();j++){
                int r1=items.characterRank[list.get(i)];
                int r2=items.characterRank[list.get(j)];
                if(r1>r2){
                    char ch=list.get(i);
                    char ch2=list.get(j);
                    list.set(i,ch2);
                    list.set(j,ch);
                }
            }
        }
        String temp="";
        for(int i=0;i<list.size();i++){
            temp=temp+list.get(i);
        }
        return temp;
    }
    void printCpTree(CpTree root){
        boolean ok=false;
        for(int i=0;i<Max;i++){
            if(root.child[i] != null){
                ok=true;
                System.out.println("character = "+(char)root.child[i].characterFromParent);
                for(int j=0;j<root.child[i].batchFrequency.size();j++){
                    System.out.print(root.child[i].batchFrequency.get(j)+" ");
                }
                System.out.println();
                printCpTree(root.child[i]);
            }
        }
        if(!ok){
            System.out.println("branch end");
        }
        
        return;
    }
    
    int totalNodeCounter(CpTree root){
        Queue<CpTree>Q = new LinkedList<CpTree>();
        Q.add(root);
        int sum = 0;
        while(Q.isEmpty() != true){
            CpTree temp = Q.poll();
            for(int i=0;i<temp.Max;i++){
                if(temp.child[i] != null){
                    Q.add(temp.child[i]);
                    sum++;
                }
            }
        }
        return sum;
    }
}

/*************************Header Table*******************************************/

class HeaderTable {
    //the list of items 
    int MAX = 258; //max number of characters
    boolean items[];//says which items is present or not
    //double vector for saving weight
    ArrayList<ArrayList<Double>> weight = new ArrayList<ArrayList<Double>>();
    //integer arraylist for saving frequency
    ArrayList<ArrayList<Integer>>frequency = new ArrayList<ArrayList<Integer>>();
    //transactions
    ArrayList<ArrayList<String>>transactions=new ArrayList<ArrayList<String>>(); 
    //number of batch
    int batch;
    
    //constructor
    public HeaderTable(){
        //memory space for weight
        items = new boolean[MAX+5];
        //memory space for weight,frequency 
        for(int i=0;i<MAX;i=i+1){
            weight.add(new ArrayList<Double>());
            frequency.add(new ArrayList<Integer>());
        }
    }
    
    //to read data from file
    void readData(String fileName){
        BufferedReader br;
        FileReader fr;
        
        try{
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            
            //number of characters
            int numberOfCharacters;
            numberOfCharacters = Integer.parseInt(br.readLine());
            //number of batches
            int numberOfBatch;
            numberOfBatch = Integer.parseInt(br.readLine());
            batch=numberOfBatch; 
            
            //for each batch
            for(int i=0;i<numberOfBatch;i++){
                //for each characters
                for(int j=0;j<numberOfCharacters;j++){
                    String character = br.readLine();
                    double weightVal = Double.parseDouble(br.readLine());
                    int index_val = character.charAt(0);
                    //this item belongs
                    this.items[index_val]=true;
                    //setting the wight
                    this.weight.get(index_val).add(weightVal);
                    //initialization of frequency
                    this.frequency.get(index_val).add(0);
                }
                //memory space allocation
                transactions.add(new ArrayList<String>());
                //number of transactions
                int numberOfTransaction;
                numberOfTransaction = Integer.parseInt(br.readLine());
                //each transaction
                for(int j=0;j<numberOfTransaction;j++){
                    //number of event
                    int numberOfEvent;
                    numberOfEvent = Integer.parseInt(br.readLine());
                    //array to save the characters
                    ArrayList<String>temp = new ArrayList<String>();
                    for(int k=0;k<numberOfEvent;k++){
                        String character = br.readLine();
                        temp.add(character);
                        //frequency increment
                        int index_val = character.charAt(0);
                        int val = frequency.get(index_val).get(i)+1;
                        frequency.get(index_val).set(i,val);
                    }
                    String var = this.arrayListToString(temp);
                    transactions.get(i).add(var);
                }
            }   
        }
        catch(Exception e){
            System.out.println("File not found");
        }
        
    }
    
    String arrayListToString(ArrayList<String>s){
        String temp="";
        for(int i=0;i<s.size();i++){
            String var = s.get(i);
            temp=temp+var;
        }
        return temp;
    }
    void printAllTransaction(){
        for(int i=0;i<transactions.size();i++){
            ArrayList<String>temp = transactions.get(i);
            System.out.println("batch "+i );
            for(int j=0;j<temp.size();j++){
                System.out.println(temp.get(j));
            }
            
        }
    }
    void printFrequency(){
        for(int i=0;i<MAX;i++){
            if(frequency.get(i).size()>0){
                char ch = (char)i;
                System.out.println(ch);
                for(int j=0;j<frequency.get(i).size();j++){
                    System.out.println(frequency.get(i).get(j)+" ");
                }
            }
        }
    }
    void printWeight(){
        for(int i=0;i<MAX;i++){
            if(weight.get(i).size()>0){
                char ch = (char)i;
                System.out.println(ch);
                for(int j=0;j<weight.get(i).size();j++){
                    System.out.println(weight.get(i).get(j));
                }
            }
        }
    }
}


/*********************************************ItemControllerClass*************************************/
class ItemControllerClass{
    int inputCharacters[]; //which characters been found
    ArrayList<PerItemInformation>list; //items and their information  
    int characterRank[];
    int Max=255;
    
    public ItemControllerClass(){
        inputCharacters = new int[Max+2];
        characterRank = new int[Max+2];
        list = new ArrayList<PerItemInformation>();
        for(int i=0;i<Max;i++){
            inputCharacters[i]=0; 
            characterRank[i] = -1;
        }
    }
    void updateIntoPerItem(int character,int currentBatchNumber){
        for(int i=0;i<list.size();i++){
            if(list.get(i).character == character){
                list.get(i).modificationBatchFrequency(list.get(i), currentBatchNumber);
                return;
            }
        }
    }
    //function to create a node for new upcoming character
    void addNodeIntoLink(int character,CpTree node,int currentBatchNumber){
        for(int i=0;i<list.size();i++){
            if(list.get(i).character ==  character){
                list.get(i).link_adress.add(node);
                return;
            }
        }
    }
    void printList(ItemControllerClass items){
        for(int i=0;i<items.list.size();i++){
            PerItemInformation obj =items.list.get(i);
            char ch = (char)obj.character;
            System.out.println(ch);
        }
    }
    void printEachItem(){
        for(int i=0;i<list.size();i++){
            System.out.println("character = "+(char)list.get(i).character+" "+list.get(i).totalAppeared+" "+list.get(i).link_adress.size());
        }
    }
}

/************PatternMining.java********************/
class IntegerObject{
    int value;
}
class DoubleObject{
    double value;
}

//Each Item Information
class ItemInfo{
    int item; //which item
    ArrayList<Integer>batchFrequency; //items frequency
    ArrayList<PatternMining>link; //link where node exists
    int totalFrequency; //total frequency count
    public ItemInfo(int character){
        item=character;
        batchFrequency=new ArrayList<Integer>();
        link=new ArrayList<PatternMining>();
        totalFrequency=0;
    }
}
//To compare two ItemInfo
class CompareItemInfo implements Comparator<ItemInfo>{

    public int compare(ItemInfo o1, ItemInfo o2) {
       
       if(o1.totalFrequency<o2.totalFrequency)return 1;
       if(o1.totalFrequency>o2.totalFrequency) return -1;
       if(o1.totalFrequency==o2.totalFrequency){
           if(o1.item>o2.item) return 1;
           return -1;
       }
       return 0;
    }
    
}

//similar to HeaderTable
class ItemTable{
    int Max=255;
    boolean items[]; //which items are present
    ArrayList<ItemInfo>itemInfos;
    public ItemTable(){
        items=new boolean[Max+1];
        itemInfos = new ArrayList<ItemInfo>();
        for(int i=0;i<Max;i++){
            items[i]=false;
        }
    }
    void printItemInfos(){
        for(int i=0;i<itemInfos.size();i++){
            System.out.println((char)itemInfos.get(i).item+" "+itemInfos.get(i).totalFrequency+" "+ itemInfos.get(i).link.size());
        }
    }
}

//class to represent generated patterns
class GeneratedItemsets{
    String frequentItemSet;
    ArrayList<Integer>freq;
    public GeneratedItemsets(){
        frequentItemSet="";
        freq=new ArrayList<Integer>();
    }
}

//To compare two ItemInfo
class CompareGeneratedItemSets implements Comparator<GeneratedItemsets>{

    public int compare(GeneratedItemsets o1, GeneratedItemsets o2) {
       char a[] = o1.frequentItemSet.toCharArray();
       Arrays.sort(a);
       char b[] = o2.frequentItemSet.toCharArray();
       Arrays.sort(b);
       String s1=new String(a);
       String s2=new String(b);
       if(s1.compareTo(s2)<0) return -1;
       if(s1.compareTo(s2)>0) return 1;
       return 0;
    }
    
}



class PatternMining {
    int Max=255;
    PatternMining child[];//child nodes
    ArrayList<Integer>batchFrequency;//batch frequency
    PatternMining parentNode; //to express who is parent
    int characterFromParent;//to say the edge character which lies between parent and child
    boolean takenNode;//node is taken or not[need to make prefix tree]
    boolean validNode; //node is valid or not[need to make conditional tree]
    double LMAXW; //LMAXW upto this node from parent
    boolean lastNode; //express this node is last or not in pattern mining
    
    int totalPatternGenerated;
    
    //constructor
    public PatternMining(){
        child = new PatternMining[Max+2];
        for(int i=0;i<Max;i++){
            child[i]=null;
        }
        batchFrequency = new ArrayList<Integer>();
        parentNode= null;
        characterFromParent=-1;
        takenNode=false; 
        validNode=true;
        LMAXW=0.0;
        lastNode=false;
        totalPatternGenerated=0;
    }
    
    //function to begin mining from here
    void beginMining(CpTree tree,PatternMining patternTree,HeaderTable headerTable,double minimumSupportThreshold){
        //copy basic tree
        DoubleObject MAXW=new DoubleObject();
        MAXW.value=0.0;
        IntegerObject maxBatchSize=new IntegerObject();
        maxBatchSize.value=0;
        ItemTable table = new ItemTable();
        //copy tree
        convertNormalTreeToPatternTree(tree,patternTree,MAXW,maxBatchSize,table,headerTable);
        //sort table
        Collections.sort(table.itemInfos, new CompareItemInfo());
        //debug: table.printItemInfos();
        
        ArrayList<GeneratedItemsets>generatedItemSets=new ArrayList<GeneratedItemsets>();

        /********DEBUG***********/
        /*boolean exit=false;
        Bruteforce b = new Bruteforce();
        b.patternGeneration(patternTree, minimumSupportThreshold, headerTable);
        /*Scanner scan = new Scanner(System.in);
        String s=scan.nextLine();
        b.stringCheckInDb(s, headerTable);
        if(exit==true) return;*/
        
        //inserting to mine
        for(int i=0;i<table.itemInfos.size();i++){
            double weight=MAXW.value*1.0*table.itemInfos.get(i).totalFrequency;
            if(weight>=minimumSupportThreshold){
                //debug: System.out.println((char)table.itemInfos.get(i).item+" "+weight);
                mining(patternTree,table.itemInfos.get(i).link,"",headerTable,minimumSupportThreshold, generatedItemSets);
            }
        }
        
        //debug: 
        //System.out.println("done "+generatedItemSets.size());
        System.out.println("done " + totalPatternGenerated);
        /*Collections.sort(generatedItemSets,new CompareGeneratedItemSets()); 
        int m=0;
        for(int i=0;i<generatedItemSets.size();i++){
            char a[] = generatedItemSets.get(i).frequentItemSet.toCharArray();
            Arrays.sort(a);
            String s=new String(a);
            System.out.println(s);
            m=Math.max(m, generatedItemSets.get(i).freq.size());
            intArrayListSizeIncrease(generatedItemSets.get(i).freq,m);
            System.out.println(generatedItemSets.get(i).freq);
            /*for(int j=0;j<generatedItemSets.get(i).freq.size();j++){
                System.out.print(generatedItemSets.get(i).freq.get(j)+" ");
            }
            System.out.println("");*/
       // }
        //debug: printPatternTree(patternTree);
    }
    
    void mining(PatternMining patternTree,ArrayList<PatternMining>lastNodes,String made,HeaderTable headerTable,double minimumSupportThreshold,ArrayList<GeneratedItemsets>generatedItemSets){
        
        //generatedPattern
        ArrayList<Integer>freq=new ArrayList<Integer>();
        for(int i=0;i<lastNodes.size();i++){
            intArrayListSizeIncrease(freq,lastNodes.get(i).batchFrequency.size());
            for(int j=0;j<lastNodes.get(i).batchFrequency.size();j++){
                int u=freq.get(j);
                int v=lastNodes.get(i).batchFrequency.get(j);
                freq.set(j,u+v);
            }
        }
        String alreadyMade=made+(char)lastNodes.get(0).characterFromParent;
        double uptoWeight=calculateWeightedSupport(alreadyMade,freq,headerTable);
        if(uptoWeight>=minimumSupportThreshold){
            //will be saved into generatedItemSets arraylist
            /**********************************************************/
            
            /*GeneratedItemsets object = new GeneratedItemsets();
            object.frequentItemSet=alreadyMade;
            object.freq=freq;
            generatedItemSets.add(object);*/
            totalPatternGenerated++;
        }
        
        //prefix Tree
        PatternMining prefixTree = new PatternMining();
        ItemTable table = new ItemTable();
        initiatePrefixTree(patternTree,prefixTree,lastNodes,table);
        //debug: printPatternTree(prefixTree);
       
        
        //sorting table
        Collections.sort(table.itemInfos, new CompareItemInfo()); 
       
        //calculation of LMAXW
        double LMAXW=0.0;
        for(int i=0;i<lastNodes.size();i++){
            LMAXW=Math.max(LMAXW, lastNodes.get(i).LMAXW);
        }
        /******SPECIAL ADDITION*****/
        LMAXW = Math.max(LMAXW,findMaxWeight(table,headerTable));
        
        
        
        //validation of nodes 
        boolean validChar[] = new boolean[Max+2];
        for(int i=0;i<Max;i++){
            validChar[i]=false;
        }
        int cnt=0;
        
        for(int i=0;i<table.itemInfos.size();i++){
            double weight=LMAXW*1.0*table.itemInfos.get(i).totalFrequency;
            if(weight>=minimumSupportThreshold){
                //debug: System.out.println("valid = "+(char)table.itemInfos.get(i).item);
                validChar[table.itemInfos.get(i).item]=true;
            }
            else{
                //debug: System.out.println("not valid = "+(char)table.itemInfos.get(i).item);
                cnt++;
                //System.out.println("LMAXW = "+LMAXW+" "+weight);
            }
        }
        
        
        //conditionalPrefixTree section
        PatternMining conditionalPrefixTree=null;
        ItemTable table2=null;
        if(cnt==0){
            //prefix Tree and conditional tree is same
            conditionalPrefixTree=prefixTree;
            table2=table;
            //debug: System.out.println("conditional Prefix Tree banano lage nai");
        }
        else{
           
            conditionalPrefixTree = new PatternMining();
            table2=new ItemTable();
            makeConditionalPrefixTree(prefixTree,conditionalPrefixTree,table2,validChar,headerTable);
            
            
            //debug: System.out.println("conditional prefix tree banaisi");
            //conditionalPrefixTree.printPatternTree(conditionalPrefixTree);
        }
        String here="";
        if(lastNodes.size() != 0){
            here=here+(char)lastNodes.get(0).characterFromParent;
        }
        for(int i=0;i<table2.itemInfos.size();i++){
            mining(conditionalPrefixTree,table2.itemInfos.get(i).link,here+made,headerTable,minimumSupportThreshold, generatedItemSets);
        }
    }
    
    //function to make conditional prefix tree
    void makeConditionalPrefixTree(PatternMining prefixTree,PatternMining conditionalPrefixTree,ItemTable table,boolean validChar[],HeaderTable headerTable){
        for(int i=0;i<Max;i++){
            if(prefixTree.child[i] != null){
                if(validChar[prefixTree.child[i].characterFromParent] == true){
                    //take this node
                    if(conditionalPrefixTree.child[i] != null){
                        //child already exists for conditional prefix tree
                        //just need to add up the frequency
                        intArrayListSizeIncrease(conditionalPrefixTree.child[i].batchFrequency,prefixTree.child[i].batchFrequency.size());
                        for(int j=0;j<prefixTree.child[i].batchFrequency.size();j++){
                            int u=prefixTree.child[i].batchFrequency.get(j);
                            int v=conditionalPrefixTree.child[i].batchFrequency.get(j);
                            conditionalPrefixTree.child[i].batchFrequency.set(j,u+v);
                        }
                        int pos=-1;
                        for(int j=0;j<table.itemInfos.size();j++){
                            if(table.itemInfos.get(j).item == i){
                                pos=j;
                                break;
                            }
                        }
                        intArrayListSizeIncrease(table.itemInfos.get(pos).batchFrequency,prefixTree.child[i].batchFrequency.size());
                        for(int j=0;j<prefixTree.child[i].batchFrequency.size();j++){
                            int u=prefixTree.child[i].batchFrequency.get(j);
                            int v=table.itemInfos.get(pos).batchFrequency.get(j);
                            table.itemInfos.get(pos).batchFrequency.set(j,u+v);
                            table.itemInfos.get(pos).totalFrequency += u;
                        }
                        makeConditionalPrefixTree(prefixTree.child[i],conditionalPrefixTree.child[i],table,validChar,headerTable);
                    }
                    else{
                        //child doesn't exist for conditional prefix tree
                        PatternMining newNode = new PatternMining();
                        newNode.parentNode=conditionalPrefixTree;
                        newNode.characterFromParent=i;
                        intArrayListSizeIncrease(newNode.batchFrequency,prefixTree.child[i].batchFrequency.size());
                        for(int j=0;j<newNode.batchFrequency.size();j++){
                            int u=newNode.batchFrequency.get(j);
                            int v=prefixTree.child[i].batchFrequency.get(j);
                            newNode.batchFrequency.set(j,u+v);
                        }
                        newNode.LMAXW=conditionalPrefixTree.LMAXW;
                        for(int j=0;j<headerTable.weight.get(i).size();j++){
                            newNode.LMAXW = Math.max(LMAXW, headerTable.weight.get(i).get(j));
                        }
                        
                        //parent
                        conditionalPrefixTree.child[i]=newNode;
                        //ItemTable 
                        if(table.items[i] == false){
                            table.items[i]=true;
                            ItemInfo object = new ItemInfo(i);
                            table.itemInfos.add(object);
                        }
                        int pos=-1;
                        for(int j=0;j<table.itemInfos.size();j++){
                            if(table.itemInfos.get(j).item == i){
                                pos=j;
                                break;
                            }
                        }
                        table.itemInfos.get(pos).link.add(newNode);
                        intArrayListSizeIncrease(table.itemInfos.get(pos).batchFrequency,prefixTree.child[i].batchFrequency.size());
                        for(int j=0;j<prefixTree.child[i].batchFrequency.size();j++){
                            int u=prefixTree.child[i].batchFrequency.get(j);
                            int v=table.itemInfos.get(pos).batchFrequency.get(j);
                            table.itemInfos.get(pos).batchFrequency.set(j,u+v);
                            table.itemInfos.get(pos).totalFrequency += u;
                        }
                        makeConditionalPrefixTree(prefixTree.child[i],conditionalPrefixTree.child[i],table,validChar,headerTable);
                    }
                }
                else{
                    //skip this node / by pass this node
                    makeConditionalPrefixTree(prefixTree.child[i],conditionalPrefixTree,table,validChar,headerTable);
                }
            }
        }
    }
    
    
    //function to initiate prefix tree
    void initiatePrefixTree(PatternMining patternTree,PatternMining prefixTree,ArrayList<PatternMining>lastNodes,ItemTable table){
        //set lastNode flag
        for(int i=0;i<lastNodes.size();i++){
            lastNodes.get(i).lastNode=true; //to mark it a last node
        }
        //identify the path by setting the takeNode as true
        for(int i=0;i<lastNodes.size();i++){
            takenNodeFlagSet(lastNodes.get(i),true);
        }
        makePrefixTree(patternTree,prefixTree,table);
        //erase path identification
        for(int i=0;i<lastNodes.size();i++){
            takenNodeFlagSet(lastNodes.get(i),false);
        }
        //set lastNode flag
        for(int i=0;i<lastNodes.size();i++){
            lastNodes.get(i).lastNode=false; //to mark it a last node
        }
        return;
    }
    
    void makePrefixTree(PatternMining patternTree,PatternMining prefixTree,ItemTable table){
       
        for(int i=0;i<Max;i++){
            if(patternTree.child[i] != null){
                if(patternTree.child[i].takenNode == true){
                    if(patternTree.child[i].lastNode == true){
                        //last node
                        if(prefixTree.parentNode != null){
                            intArrayListSizeIncrease(prefixTree.batchFrequency,patternTree.child[i].batchFrequency.size());
                            //no reason to add in root node
                            for(int j=0;j<patternTree.child[i].batchFrequency.size();j++){
                                int u=patternTree.child[i].batchFrequency.get(j);
                                int v=prefixTree.batchFrequency.get(j);
                                prefixTree.batchFrequency.set(j,u+v);
                            }
                            //change in table
                            int pos=-1;
                            for(int j=0;j<table.itemInfos.size();j++){
                                if(table.itemInfos.get(j).item == prefixTree.characterFromParent){
                                    pos=j;
                                    break;
                                }
                            }
                            intArrayListSizeIncrease(table.itemInfos.get(pos).batchFrequency,patternTree.child[i].batchFrequency.size());
                            for(int j=0;j<patternTree.child[i].batchFrequency.size();j++){
                                int u=patternTree.child[i].batchFrequency.get(j);
                                int v=table.itemInfos.get(pos).batchFrequency.get(j);
                                table.itemInfos.get(pos).batchFrequency.set(j,u+v);
                                table.itemInfos.get(pos).totalFrequency += u;
                            }
                        }
                    }
                    else if(patternTree.child[i].lastNode == false){
                        //child
                        PatternMining newNode= new PatternMining();
                        newNode.parentNode=prefixTree;
                        newNode.characterFromParent=patternTree.child[i].characterFromParent;
                        newNode.LMAXW = patternTree.child[i].LMAXW;
                        
                        //parent
                        prefixTree.child[i]=newNode;
                        if(table.items[i] == false){
                            table.items[i]=true;
                            ItemInfo object = new ItemInfo(i);
                            table.itemInfos.add(object);
                        }
                        int pos=-1;
                        for(int j=0;j<table.itemInfos.size();j++){
                            if(table.itemInfos.get(j).item == i){
                                pos=j;
                                break;
                            }
                        }
                        table.itemInfos.get(pos).link.add(newNode);
                        makePrefixTree(patternTree.child[i],prefixTree.child[i],table);
                        //frequency will be added to parent
                        if(prefixTree.parentNode != null){
                            intArrayListSizeIncrease(prefixTree.batchFrequency,prefixTree.child[i].batchFrequency.size());
                            //no reason to add in root node
                            for(int j=0;j<prefixTree.child[i].batchFrequency.size();j++){
                                int u=prefixTree.child[i].batchFrequency.get(j);
                                int v=prefixTree.batchFrequency.get(j);
                                prefixTree.batchFrequency.set(j,u+v);
                            }
                            //change in table
                            pos=-1;
                            for(int j=0;j<table.itemInfos.size();j++){
                                if(table.itemInfos.get(j).item == prefixTree.characterFromParent){
                                    pos=j;
                                    break;
                                }
                            }
                            intArrayListSizeIncrease(table.itemInfos.get(pos).batchFrequency,prefixTree.child[i].batchFrequency.size());
                            for(int j=0;j<prefixTree.child[i].batchFrequency.size();j++){
                                int u=prefixTree.child[i].batchFrequency.get(j);
                                int v=table.itemInfos.get(pos).batchFrequency.get(j);
                                table.itemInfos.get(pos).batchFrequency.set(j,u+v);
                                table.itemInfos.get(pos).totalFrequency += u;
                            }
                        }
                    }
                }
            }
        }
        return;
    }
    
    //function to set the takenNode Flag
    void takenNodeFlagSet(PatternMining lastNodePosition,boolean setValue){
        if(lastNodePosition == null) {
            return;
        }
        lastNodePosition.takenNode=setValue;
        takenNodeFlagSet(lastNodePosition.parentNode,setValue);
    }
    
    
    //function to covert normal tree to pattern tree
    void convertNormalTreeToPatternTree(CpTree tree,PatternMining patternTree,DoubleObject maxW,IntegerObject maxBatch,ItemTable table,HeaderTable headerTable){
        
        for(int i=0;i<Max;i++){
            if(tree.child[i] != null){
                //format child
                PatternMining newNode=new PatternMining();
                for(int j=0;j<tree.child[i].batchFrequency.size();j++){
                    newNode.batchFrequency.add(tree.child[i].batchFrequency.get(j));
                }
                newNode.parentNode = patternTree;
                newNode.characterFromParent=i;
                
                double lmaxw=patternTree.LMAXW;
                
                for(int j=0;j<headerTable.weight.get(i).size();j++){
                    lmaxw=Math.max(lmaxw, headerTable.weight.get(i).get(j));
                }
                newNode.LMAXW= lmaxw;
                //link with parent
                patternTree.child[i]=newNode;
                //update of maxW and maxBatch
                maxW.value = Math.max(maxW.value,newNode.LMAXW);
                maxBatch.value=Math.max(maxBatch.value,newNode.batchFrequency.size());
                //update of itmeTable table
                if(table.items[i]== false){
                   table.items[i]=true;
                   ItemInfo object = new ItemInfo(i);
                   table.itemInfos.add(object);
                }
                int pos=-1;
                for(int j=0;j<table.itemInfos.size();j++){
                    if(table.itemInfos.get(j).item == i){
                        pos=j;
                        break;
                    }
                }
                //size increase
                intArrayListSizeIncrease(table.itemInfos.get(pos).batchFrequency,maxBatch.value);
                //sum
                for(int j=0;j<newNode.batchFrequency.size();j++){
                    int v=newNode.batchFrequency.get(j);
                    int u=table.itemInfos.get(pos).batchFrequency.get(j);
                    table.itemInfos.get(pos).batchFrequency.set(j,v+u);
                    table.itemInfos.get(pos).totalFrequency += v;
                }
                //link add
                table.itemInfos.get(pos).link.add(newNode);
                //recursion
                convertNormalTreeToPatternTree(tree.child[i],patternTree.child[i],maxW,maxBatch,table,headerTable);
            }
        }
    }
    
    //function to calculate weighted support
    double calculateWeightedSupport(String s,ArrayList<Integer>freq,HeaderTable headerTable){
        double weight=0.0;
        for(int i=0;i<freq.size();i++){
            double sum=0.0;
            for(int j=0;j<s.length();j++){
                int int_val=s.charAt(j);
                sum= sum+headerTable.weight.get(int_val).get(i);
            }
            sum=sum/(double)(s.length());
            sum=sum*freq.get(i)*1.0;
            weight+= sum;
        }
        return weight;
    }
    
    void intArrayListSizeIncrease(ArrayList<Integer>list,int maxSize){
        while(list.size()<maxSize){
            list.add(0);
        }
        return;
    }
    
    void printPatternTree(PatternMining patternTree){
        for(int i=0;i<Max;i++){
            if(patternTree.child[i] != null){
                System.out.println((char)i);
                //debug: int sum=0;
                for(int j=0;j<patternTree.child[i].batchFrequency.size();j++){
                    System.out.print(patternTree.child[i].batchFrequency.get(j)+" ");
                    //debug: sum += patternTree.child[i].batchFrequency.get(j);
                }
                System.out.println("");
                //debug: System.out.println("sum = "+sum);
                System.out.println("branch start");
                printPatternTree(patternTree.child[i]);
                System.out.println("brnach end");
            }
        }
    }
    
    boolean checkingSanityOfTable(PatternMining prefixTree,ItemTable check){
        Queue<PatternMining>Q=new LinkedList<PatternMining>();
        Q.add(prefixTree);
        ItemTable cal=new ItemTable();
        while(Q.isEmpty()!= true){
            PatternMining v = Q.poll();
            for(int i=0;i<Max;i++){
                if(v.child[i] != null){
                    if(cal.items[i] == false){
                        cal.items[i]=true;
                        ItemInfo obj=new ItemInfo(i);
                        cal.itemInfos.add(obj);
                    }
                    for(int j=0;j<cal.itemInfos.size();j++){
                        if(cal.itemInfos.get(j).item==i){
                            cal.itemInfos.get(j).link.add(v.child[i]);
                            intArrayListSizeIncrease(cal.itemInfos.get(j).batchFrequency,v.child[i].batchFrequency.size());
                            for(int k=0;k<v.child[i].batchFrequency.size();k++){
                                int p=cal.itemInfos.get(j).batchFrequency.get(k);
                                int q=v.child[i].batchFrequency.get(k);
                                cal.itemInfos.get(j).batchFrequency.set(k, p+q);
                                cal.itemInfos.get(j).totalFrequency += q;
                            }
                            break;
                        }
                    }
                    Q.add(v.child[i]);
                }
            }
        }
        if(cal.itemInfos.size() != check.itemInfos.size()) return false;
        for(int i=0;i<cal.itemInfos.size();i++){
            for(int j=0;j<check.itemInfos.size();j++){
                if(cal.itemInfos.get(i).item == check.itemInfos.get(j).item) {
                    
                    if(cal.itemInfos.get(i).link.size() == check.itemInfos.get(j).link.size()){
                        if(cal.itemInfos.get(i).batchFrequency.size() != check.itemInfos.get(j).batchFrequency.size()) {
                            return false;
                        }
                        boolean ok=true;
                        int sum=0;
                        for(int k=0;k<cal.itemInfos.get(i).batchFrequency.size();k++){
                            if(cal.itemInfos.get(i).batchFrequency.get(k) == check.itemInfos.get(j).batchFrequency.get(k)) {
                                sum += cal.itemInfos.get(i).batchFrequency.get(k);
                                continue;
                            }
                            else{
                                ok=false;
                                break;
                            }
                        }
                        if(ok==true){
                            break;
                        }
                        else if(sum != cal.itemInfos.get(i).totalFrequency){
                            System.out.println("hai hai");
                            return false;
                        }
                        else{
                            
                            System.out.println("first "+cal.itemInfos.get(i).batchFrequency);
                            System.out.println("second "+check.itemInfos.get(j).batchFrequency);
                            return false;
                        }
                    }
                    else{
                                                
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    double findMaxWeight(ItemTable table,HeaderTable header){
        double result=0;
        //System.out.println(table.itemInfos.size()+"hu");
        for(int i=0;i<table.itemInfos.size();i++){
            int it=table.itemInfos.get(i).item;
            for(int j=0;j<header.weight.get(it).size();j++){
              result = Math.max(result, header.weight.get(it).get(j));
            }
        }
        return result;
    }
}

/**********Per item information.java****/

class PerItemInformation{
    int character; //which character
    ArrayList<Integer>batchFrequency = new ArrayList<Integer>(); //frequency in each batch
    ArrayList<CpTree>link_adress = new ArrayList<CpTree>(); //to save the address where the node
     //of this character belongs
    int totalAppeared; //number of times this character appeared
    public PerItemInformation(int int_val){
        totalAppeared = 0; 
        character = int_val; 
    }
    //function to add into batch frquency
    //so that character's total frequency remain balanced
    void modificationBatchFrequency(PerItemInformation object,int currentBatchNumber){
        while(object.batchFrequency.size()<currentBatchNumber){
            object.batchFrequency.add(0); 
        }
        int v = object.batchFrequency.get(currentBatchNumber-1);
        v++;
        object.totalAppeared++;
        object.batchFrequency.set(currentBatchNumber-1,v); 
        return;
    }
    //function to delete redundant links
    void deleteRedundantLinkAddress(PerItemInformation obj){
        ArrayList<CpTree>temp = new ArrayList<CpTree>();
        for(int i=0;i<obj.link_adress.size();i++){
            if(obj.link_adress.get(i).parent != null){
                //valid node
                temp.add(obj.link_adress.get(i));
            }
        }
        obj.link_adress = temp;
        return;
    }
}








