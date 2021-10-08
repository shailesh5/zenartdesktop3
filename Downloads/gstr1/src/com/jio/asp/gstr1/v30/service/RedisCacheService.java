/**
 * 
 */
package com.jio.asp.gstr1.v30.service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author amit1.dwivedi
 *
 */
public interface RedisCacheService {

	String save(Map<String, Object> itemDtlMap, String secNAme);

	public Map<Object, Object> findAll(String key);

	Map<String, Object> find(String key, String id);


	String saveAll(Map<String, Object> itemDtlMap, Map<String, String> data);

	boolean chekcKey(String string);

	Map<Object, Object> getPagedData(Map<String, String> data, Set<Object> hk);


}
