#purpose of this file is to make itemsets from a text file. Each line will
#correspond to an itemset and save that into transactions.txt 
#also will make batches of transactions and will give weight and save into batch_transactions.txt 

import random


file_name = raw_input("Give input file name to discretize: ")

f=open(file_name,"r")

list_set=[] #each line
#reading each line and saving into list 
for line in f:
    list_set.append(line)
f.close()

#splitting + saving the numbers 
itemset = []
max_val = -100000000000000000000000000000000000000000000000000
min_val = 1000000000000000000000000000000000000000000000000000  
for line in list_set:
    string_list = line.split() #removing the space and getting out the numbers
    if(len(string_list) == 0):
        continue
    number_list = []
    for j in string_list:
        number_list.append(int(j))
        max_val = max(max_val,int(j)) 
        min_val = min(min_val,int(j)) 
    itemset.append(number_list)

#range division
diff = max_val-min_val+1
print "max_value in the dataset: ",max_val," min_value in the dataset: ",min_val
number_of_symbol = int(raw_input("Give in how many ranges numbers will be divided(Max 22,Min 1):  "))
range_amount = diff/number_of_symbol #how many divisions need to be done like for 5 a,b,c,d,e and range_amount says each range will have this amount of number

f=open("transactions.txt","w")
  
symbol_list = ['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z']

total_transactions = 0
transactions = []

for items in itemset:
    item_list=[]
    #taking each item and discretizing it 
    for i in items:
        for j in range(1,range_amount+3):
            if(i<j*range_amount):
                #do here
                element = symbol_list[j-1]
                if(element not in item_list):
                    item_list.append(element)
                break
            else:
                continue
    string=""
    for i in range(0,len(item_list)):
        if(i==0):
            string=item_list[i]
        elif(i==len(item_list)-1):
            string=string+" "+item_list[i]
            #print item_list[i]
            #f.write(item_list[i])
        else:
            string=string+" "+item_list[i]
            #print item_list[i],
            #f.write(item_list[i],)
    result = string 
    string = string + "\n"
    total_transactions=total_transactions+1
    transactions.append(result)
    f.write(string)
f.close()

print "Transactions are saved into 'transactions.txt' file "
print "number of total transactions: ",total_transactions
number_of_transaction_in_batch = int(raw_input("Give Max Number of transaction in a batch: "))

#couting total number of batch
total_number_of_batch = (total_transactions/number_of_transaction_in_batch)
if((total_transactions%number_of_transaction_in_batch) != 0):
    total_number_of_batch = total_number_of_batch+1

#file for viewers 
f1 = open("weighted transactions.txt","w")
f1.write(str(number_of_symbol+1))
f1.write("\n")
f1.write(str(total_number_of_batch))
f1.write("\n")
f2 = open("processed transactions.txt","w")
f2.write(str(number_of_symbol+1))
f2.write("\n")
f2.write(str(total_number_of_batch))
f2.write("\n")



def weight_generation(local_transacions_list):
    symbol_counter = {} #a dictionary to count symbols
    result_weight = {}
    for i in symbol_list:
        symbol_counter[i] = 0 #initialize
        result_weight[i] = 0
    local_counter = 0
    for i in local_transacions_list:
        for j in i:
            if(j == ' '):
                continue
            symbol_counter[j] = symbol_counter[j] + 1
            if(symbol_counter[j] == 1):
                local_counter=local_counter+1
    random_numbers = []
    #randomization section
    while len(random_numbers)<local_counter:
        #do here
        val =  random.randint(1,100)
        if val not in random_numbers:
            random_numbers.append(val)
    random_numbers.sort()
    for i in range(0,len(random_numbers)):
        random_numbers[i] = random_numbers[i]/(100.0)
    
    i=len(random_numbers)-1
    while i>=0:
        #do here
        base = 'a'
        for j in symbol_counter:
            if(symbol_counter[j]>symbol_counter[base]):
                base = j 
        symbol_counter[base]=0
        result_weight[base] = random_numbers[i] 
        i=i-1
    return result_weight



i=0
batch_count = 0
while i<len(transactions):
    counter = 0 #how many transactions have been count for this batch
    local_transacions_list = []
    batch_count=batch_count+1
    while True:
        if(i>=len(transactions)):
            break
        counter=counter+1
        if(counter > number_of_transaction_in_batch):
            break
        local_transacions_list.append(transactions[i])
        i=i+1
    result_weight = weight_generation(local_transacions_list)
    for j in range(1,number_of_symbol+2):
        f2.write(symbol_list[j-1])
        f2.write("\n")
        f2.write(str(result_weight[symbol_list[j-1]]))
        f2.write("\n")
    print local_transacions_list," ",len(local_transacions_list)
    f2.write(str(len(local_transacions_list))+"\n")
    for j in local_transacions_list:
        temp = j.split()
        f2.write(str(len(temp)))
        f2.write('\n')
        for k in temp:
            f2.write(k)
            f2.write('\n')
    f1.write("Batch Number: "+str(batch_count)+"\n")
    for j in range(1,number_of_symbol+2):
        f1.write(symbol_list[j-1])
        f1.write(" ")
    f1.write("\n")
    for j in range(1,number_of_symbol+2):
        f1.write(str(result_weight[symbol_list[j-1]]))
        f1.write(" ")
    f1.write("\n")
    f1.write(str(len(local_transacions_list))+"\n")
    for j in local_transacions_list:
        f1.write(j+"\n")    

print "Easily Undestandable Result can be seen in weighted transactions.txt "
print "Code readable format output is generated in processed transactions.txt,this file should be given to code"
f2.close()
f1.close()


    

         


