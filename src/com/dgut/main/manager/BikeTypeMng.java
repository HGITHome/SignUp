package com.dgut.main.manager;

import java.util.List;
import com.dgut.main.entity.BikeType;

public interface BikeTypeMng {
	public List<BikeType> getList();

	public BikeType findById(Integer id);

	public BikeType save(BikeType bean);

	public BikeType update(BikeType bean);

	public BikeType deleteById(Integer id);

	public BikeType[] deleteByIds(Integer[] ids);
}