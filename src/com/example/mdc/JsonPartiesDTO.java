package com.example.mdc;

import java.util.ArrayList;
import java.util.List;

public class JsonPartiesDTO {
	
	public List<JsonPartiesRec> partiesRecs;
	
	public JsonPartiesDTO(){
		partiesRecs = new ArrayList<JsonPartiesRec>();
	}
}
