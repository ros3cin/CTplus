package br.ufpe.cin.dataanalysis;

public enum Complexity {
	O1, OLOGN , ON,ONLOGN, ON2, ON3, O2N , INFINITE;
	
	public String getComplexity(Complexity com1,Complexity com2){
		
		String retorno = ""; 
		
		switch (com1) {
        case O1:
            System.out.println("O1");            
            retorno = com2.name();
                        
            break;
                
        case OLOGN:
            System.out.println("OLOGN");
            break;
                     
        case ON:
        	if(com2.name().equals(O1)){
        		
        	}
        
        case ONLOGN:
            System.out.println("ONLOGN");
            break;
            
        case ON2:
            System.out.println("ON2");
            break;
            
            
                    
        default:
            System.out.println("Not defined");
            break;
    }
		
		return retorno;
		
	}
}
