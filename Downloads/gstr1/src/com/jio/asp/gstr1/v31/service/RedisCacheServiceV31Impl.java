package com.jio.asp.gstr1.v31.service;

import java.util.ArrayList;
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

import com.jio.asp.gstr1.v31.constant.Gstr1ConstantsV31;

/**
 * @author Amit1.Dwivedi
 *
 */
@Service
public class RedisCacheServiceV31Impl implements RedisCacheServiceV31 {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	@Autowired
	private MessageSource gstnResource;

	@Override
	public String save(Map<String, Object> itemDtlMap, String secNAme) {
		String redisKey = (String) itemDtlMap.get(Gstr1ConstantsV31.INPUT_ACK_NO);
		String expTime = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_REDIS_EXP_TIME, null,
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
		String expTime = gstnResource.getMessage(Gstr1ConstantsV31.GSTN_REDIS_EXP_TIME, null,
				LocaleContextHolder.getLocale());
		String redisKey = (String) data.get(Gstr1ConstantsV31.INPUT_ACK_NO);
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
		String stId=(String) data.get(Gstr1ConstantsV31.STORE_ID_IN_DATA);
		List<Object> recordsList = new ArrayList<>();
		Long size = redisTemplate.opsForHash().size(data.get(Gstr1ConstantsV31.INPUT_ACK_NO));
		List<Object> list = redisTemplate.opsForHash().multiGet(data.get(Gstr1ConstantsV31.INPUT_ACK_NO), hk);
		list.removeAll(Collections.singleton(null));
		if (Gstr1ConstantsV31.TYPE_NIL.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> innerMap = new HashMap<>();
				innerMap = (Map<Object, Object>) list.get(i);
				List<Object> l2 = new ArrayList<>();
				l2.add(innerMap);
				// change regarding new structure
//				Map<String, List<Object>> itemMap = new HashMap<>();
				// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
				// l2);

				Map<String, Object> itemMap = new HashMap<>();
				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
					itemMap.put("id", stId);
					itemMap.put("rates", l2);
				} else {
					itemMap.put(stId, l2);
				}
//				itemMap.put(stId, l2);
				recordsList.add(itemMap);
				// recordsList.add(l2);
			}

		}

		else if (Gstr1ConstantsV31.TYPE_B2CS.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> innerMap = new HashMap<>();
				innerMap = (Map<Object, Object>) list.get(i);
				List<Object> l2 = new ArrayList<>();
				l2.add(innerMap);
				// change regarding new structure
				Map<String, Object> itemMap = new HashMap<>();

				// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
				// l2);
				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
					itemMap.put("id", stId);
					itemMap.put("rates", l2);
				} else {
					itemMap.put(stId, l2);
				}

				recordsList.add(itemMap);
				// recordsList.add(l2);
			}

		} else if (Gstr1ConstantsV31.TYPE_AT.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> innerMap = new HashMap<>();
				innerMap = (Map<Object, Object>) list.get(i);
				List<Object> l2 = new ArrayList<>();
				l2.add(innerMap);
//				Map<String, List<Object>> itemMap = new HashMap<>();
				// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
				// l2);
				Map<String, Object> itemMap = new HashMap<>();
				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
					itemMap.put("id", stId);
					itemMap.put("rates", l2);
				} else {
					itemMap.put(stId, l2);
				}
//				itemMap.put(stId, l2);
				recordsList.add(itemMap);

			}

		} else if (Gstr1ConstantsV31.TYPE_CDNUR.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))
				|| Gstr1ConstantsV31.TYPE_CDNR.equalsIgnoreCase((String) data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> innerMap = new HashMap<>();
				innerMap = (Map<Object, Object>) list.get(i);
				recordsList.add(innerMap);

			}

		} else if (Gstr1ConstantsV31.TYPE_TXPD.equals((String) data.get(Gstr1ConstantsV31.INPUT_SECTION) + "d")
				|| Gstr1ConstantsV31.TYPE_DOCS_L2.equalsIgnoreCase(data.get(Gstr1ConstantsV31.INPUT_SECTION))) {
			for (int i = 0; i < list.size(); i++) {
				Map<Object, Object> innerMap = new HashMap<>();
				List<Object> l2 = new ArrayList<>();
				innerMap = (Map<Object, Object>) list.get(i);
//				Map<String, List<Object>> itemMap = new HashMap<>();
				// itemMap.put(Gstr1ConstantsV31.CONTROL_JSON_CUST_SEC_L2_List,
				// l2);
				
				Map<String, Object> itemMap = new HashMap<>();
				if (!(data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP) == null)
						&& (data.get(Gstr1ConstantsV31.INPUT_STRUCTURE_TYP).equalsIgnoreCase("2"))) {
					itemMap.put("id", stId);
					itemMap.put("rates", l2);
				} else {
					itemMap.put(stId, l2);
				}
//				itemMap.put(stId, l2);
				recordsList.add(innerMap);
			}

		}
		// else if (Gstr1ConstantsV31.TYPE_TXPD.equals((String)
		// data.get(Gstr1ConstantsV31.INPUT_SECTION) + "d")
		// ||
		// Gstr1ConstantsV31.TYPE_DOCS_L2.equalsIgnoreCase(data.get(Gstr1ConstantsV31.INPUT_SECTION)))
		// {
		// for (int i = 0; i < list.size(); i++) {
		// Map<Object, Object> inerList = new HashMap<>();
		// inerList = (Map<Object, Object>) list.get(i);
		// recordsList.add(inerList);
		// }
		//
		// }
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
		result.put("gstin", data.get(Gstr1ConstantsV31.INPUT_GSTN));
		result.put("fp", data.get(Gstr1ConstantsV31.INPUT_FP));
		// result.put("curr_gt", "");
		// result.put("sname", "");
		// result.put("gt", "");
		return result;
	}

}
