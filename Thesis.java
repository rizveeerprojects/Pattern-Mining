import java.util.*;
import java.io.*; 

/*************Thesis.java********************/
public class Thesis {
    public static void main(String[] args) {
        // TODO code application logic here
        HeaderTable headerTable = new HeaderTable();
        //reading data from file
        headerTable.readData("processed transactions.txt");
        //generating FpTree
        double clockTime = System.currentTimeMillis();
        FpTree obj = new FpTree();
        obj.generateFpTree(headerTable);
        double res = System.currentTimeMillis()-clockTime;
        double val = obj.nodeCount(obj);
        System.out.println("total node count: "+val);
        System.out.println("total time to make tree: "+res);
        
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
        
        //obj.printFpTree(obj);
        //mining Pattern
        clockTime = System.currentTimeMillis();
        PatternMining pattern = new PatternMining();
        pattern.beginMining(obj, pattern, headerTable,minimumSupportThreshold);
        res = System.currentTimeMillis()-clockTime;
        System.out.println("Total time to generate patterns: "+res);
    }
    
}


/***************FpTree.java******************************/
class FpTree {
    int Max=258;
    //to save the address of child node
    FpTree child[]=new FpTree[Max+5];
    //frequency vector
    ArrayList<Integer>batchFrequency = new ArrayList<Integer>();
    //parent
    FpTree parent;
    //int character from parent
    int characterFromParent; //the character through it got down
    //to identify if dfs got or not
    boolean touched;
    //last node definition
    boolean lastNode; 
    //valid node check
    boolean validNode; 
    
    public FpTree(){
        for(int i=0;i<Max;i++){
            child[i]=null;
        }
        parent=null;
        touched=false; //means BFS from reverse hasn't touched this node
        lastNode=false;
    }
    void generateFpTree(HeaderTable h){
      //for each batch
     // System.out.println(h.batch);
      for(int i=0;i<h.batch;i++){
         // System.out.print("i = "+i+h.transactions.get(i))
          //for each transaction
          for(int j=0;j<h.transactions.get(i).size();j++){
              //string which will be inserted into the tree
              String s = h.transactions.get(i).get(j);
              this.addTransaction(s, 0, this,h.batch,i);
          }
      }
    }
    void addTransaction(String s, int index,FpTree obj,int batchTotal, int currentBatchNo){
     //function to add a transaction to the FpTree
     //s = which will string will be added
     //index = the index of s with which we are working
     //FpTree = node
     //batchTotal = number of total batch in the input
     //currentBatchNo = the batch with which we are working
     if(index>=s.length()){
         return;
     }
     int int_val = s.charAt(index);
     if(obj.child[int_val] == null){
         //node creation with link
        obj.child[int_val] = new FpTree();
        //initialization of batch frquency
        for(int i=0;i<batchTotal;i++){
            obj.child[int_val].batchFrequency.add(0);
        }
        obj.child[int_val].batchFrequency.set(currentBatchNo, 1);
        obj.child[int_val].parent = obj;//parent initiation
        characterFromParent = int_val; //the character through which it came from parent
     }
     else{
       //node already exists
       int val = 1+obj.child[int_val].batchFrequency.get(currentBatchNo);
       obj.child[int_val].batchFrequency.set(currentBatchNo, val);
     }
     addTransaction(s,index+1,obj.child[int_val],batchTotal,currentBatchNo);
     return;
    }
    
    void printFpTree(FpTree obj){
        for(int i=0;i<Max;i++){
            if(obj.child[i] != null){
                char ch = (char)i;
                System.out.println(ch);
                for(int j=0;j<obj.child[i].batchFrequency.size();j++){
                    System.out.print(obj.child[i].batchFrequency.get(j)+" ");
                }
                System.out.println("");
                printFpTree(obj.child[i]);
            }
        }
    }
    
    int nodeCount(FpTree obj){
        Queue<FpTree>Q = new LinkedList<FpTree>();
        Q.add(obj);
        int sum=0;
        while(Q.isEmpty() != true){
            FpTree u = Q.peek();
            Q.remove();
            for(int i=0;i<Max;i++){
                if(u.child[i]!= null){
                    Q.add(u.child[i]);
                    sum++;
                }
            }
        }
        return sum;
    }
    
}

/*****************HeaderTable.java***********************/
class HeaderTable {
    //the list of items 
    int MAX = 255; //max number of characters
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
        Collections.sort(s);
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

/*************PatternMining.java**********************/
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
       
       if(o1.item<o2.item) return -1;
       if(o1.item>o2.item) return 1;
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

//To compare two GeneratedItemSets
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
    void beginMining(FpTree tree,PatternMining patternTree,HeaderTable headerTable,double minimumSupportThreshold){
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
        //}
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
           // conditionalPrefixTree.printPatternTree(conditionalPrefixTree);
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
    void convertNormalTreeToPatternTree(FpTree tree,PatternMining patternTree,DoubleObject maxW,IntegerObject maxBatch,ItemTable table,HeaderTable headerTable){
        
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
                for(int j=0;j<patternTree.child[i].batchFrequency.size();j++){
                    System.out.print(patternTree.child[i].batchFrequency.get(j)+" ");
                }
                System.out.println("");
                printPatternTree(patternTree.child[i]);
            }
        }
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



