select d.name as name, tkd.key as key, tk.bool_v,
       tk.long_v, tk.dbl_v, tk.ts as timestamp from asset a
    join relation r on a.id = r.from_id
    join device d on d.id = r.to_id
    join ts_kv tk on tk.entity_id = r.to_id
    join ts_kv_dictionary tkd on tk.key = tkd.key_id
where from_type = 'ASSET' and to_type = 'DEVICE'
  and tk.ts = (select max(tk.ts) from asset a
                      join relation r on a.id = r.from_id
                      join device d on d.id = r.to_id
                      join ts_kv tk on tk.entity_id = r.to_id
                      join ts_kv_dictionary tkd on tk.key = tkd.key_id
               where from_type = 'ASSET' and to_type = 'DEVICE'
                 and (tk.ts >= :startRangeTime or tk.ts < :endTime)
                 and a.tenant_id = :tentenant_id and a.id = :assert_id)
  and (tk.ts >= :startRangeTime or tk.ts < :endTime)
  and a.tenant_id = :tentenant_id and a.id = :assert_id;