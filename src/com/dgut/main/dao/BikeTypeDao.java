package com.dgut.main.dao;

import java.util.List;
import com.dgut.common.hibernate3.Updater;
import com.dgut.main.entity.BikeType;

public interface BikeTypeDao {
	public List<BikeType> getList();

	public BikeType findById(Integer id);

	public BikeType save(BikeType bean);

	public BikeType updateByUpdater(Updater<BikeType> updater);

	public BikeType deleteById(Integer id);
}