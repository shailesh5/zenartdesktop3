package com.jio.asp.gstr1.v30.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.jio.asp.gstr1.v30.constant.Gstr1Constants;

/**
 * @author Amit1.Dwivedi
 *
 */
@Service
public class RedisCacheServiceImpl implements RedisCacheService {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	@Autowired
	private MessageSource gstnResource;

	@Override
	public String save(Map<String, Object> itemDtlMap, String secNAme) {
		String redisKey = (String) itemDtlMap.get(Gstr1Constants.INPUT_ACK_NO);
		String expTime = gstnResource.getMessage(Gstr1Constants.GSTN_REDIS_EXP_TIME, null,
				LocaleContextHolder.getLocale());

		for (Entry<String, Object> entery : itemDtlMap.entrySet()) {
			this.redisTemplate.opsForHash().put(redisKey, entery.getKey(), entery.getValue());
			redisTemplate.expire(redisKey, Long.parseLong(expTime), TimeUnit.MINUTES);

		}
		return redisKey;
	}

	@Override
	public Map<String, Object> find(String key, String id) {
		return (Map<String, Object>) this.redisTemplate.opsForHash().get(key, id);
	}

	@Override
	public Map<Object, Object> findAll(String key) {
		return this.redisTemplate.opsForHash().entries(key);
	}

	@Override
	public String saveAll(Map<String, Object> itemDtlMap, Map<String, String> data) {
		String expTime = gstnResource.getMessage(Gstr1Constants.GSTN_REDIS_EXP_TIME, null,
				LocaleContextHolder.getLocale());
		String redisKey = (String) data.get(Gstr1Constants.INPUT_ACK_NO);
		this.redisTemplate.opsForHash().putAll(redisKey, itemDtlMap);
		redisTemplate.expire(redisKey, Long.parseLong(expTime), TimeUnit.MINUTES);

		return redisKey;
	}

	@Override
	public boolean chekcKey(String string) {
		return redisTemplate.hasKey(string);
	}

	@Override
	public Map<Object, Object> getPagedData(Map<String, String> data, Set<Object> hk) {

		List<Object> recordsList = new ArrayList<>();
		Long size = redisTemplate.opsForHash().size(data.get(Gstr1Constants.INPUT_ACK_NO));
		List<Object> list = redisTemplate.opsForHash().multiGet(data.get(Gstr1Constants.INPUT_ACK_NO), hk);
		list.removeAll(Collections.singleton(null));
		if (Gstr1Constants.TYPE_NIL.equals((String) data.get(Gstr1Constants.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> innerMap = new HashMap<>();
				innerMap = (Map<Object, Object>) list.get(i);
				List<Object> l2 = new ArrayList<>();
				l2.add(innerMap);
				//change regarding new structure 
				Map<String, List<Object>> itemMap = new HashMap<>();
				itemMap.put(Gstr1Constants.CONTROL_JSON_CUST_SEC_L2_List, l2);
				recordsList.add(itemMap);
//				recordsList.add(l2);
			}

		}

		else if (Gstr1Constants.TYPE_B2CS.equals((String) data.get(Gstr1Constants.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> innerMap = new HashMap<>();
				innerMap = (Map<Object, Object>) list.get(i);
				List<Object> l2 = new ArrayList<>();
				l2.add(innerMap);
				//change regarding new structure 
				Map<String, List<Object>> itemMap = new HashMap<>();
				itemMap.put(Gstr1Constants.CONTROL_JSON_CUST_SEC_L2_List, l2);
				recordsList.add(itemMap);
//				recordsList.add(l2);
			}

		} else if (Gstr1Constants.TYPE_AT.equals((String) data.get(Gstr1Constants.INPUT_SECTION))
				|| Gstr1Constants.TYPE_CDNUR.equals((String) data.get(Gstr1Constants.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> innerMap = new HashMap<>();
				innerMap = (Map<Object, Object>) list.get(i);
				recordsList.add(innerMap);

			}

		} else if (Gstr1Constants.TYPE_TXPD.equals((String) data.get(Gstr1Constants.INPUT_SECTION) + "d")
				|| Gstr1Constants.TYPE_DOCS_L2.equalsIgnoreCase(data.get(Gstr1Constants.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> inerList = new HashMap<>();
				inerList = (Map<Object, Object>) list.get(i);
				recordsList.add(inerList);
			}

		} 
		else if (Gstr1Constants.TYPE_TXPD.equals((String) data.get(Gstr1Constants.INPUT_SECTION) + "d")
				|| Gstr1Constants.TYPE_DOCS_L2.equalsIgnoreCase(data.get(Gstr1Constants.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> inerList = new HashMap<>();
				inerList = (Map<Object, Object>) list.get(i);
				recordsList.add(inerList);
			}

		} 
		else {
			for (int i = 0; i < list.size(); i++) {
				List<Object> inerList = (List<Object>) list.get(i);
				for (int j = 0; j < inerList.size(); j++) {
					Map<Object, Object> recMap = new HashMap<>();
					recMap = (Map<Object, Object>) inerList.get(j);
					recordsList.add(recMap);
				}
			}
		}
		Map<Object, Object> result = new HashMap<>();
		result.put("record", recordsList);
		result.put("ttl_record", size);
		result.put("gstin", data.get(Gstr1Constants.INPUT_GSTN));
		result.put("fp", data.get(Gstr1Constants.INPUT_FP));
		result.put("curr_gt", "");
		result.put("sname", "");
		result.put("gt", "");
		return result;
	}

}
