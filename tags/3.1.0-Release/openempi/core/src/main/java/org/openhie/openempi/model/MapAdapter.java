/**
 *
 * Copyright (C) 2002-2012 "SYSNET International, Inc."
 * support@sysnetint.com [http://www.sysnetint.com]
 *
 * This file is part of OpenEMPI.
 *
 * OpenEMPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openhie.openempi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

@XmlSeeAlso({ MapAdapter.Adapter.class, MapAdapter.MapElement.class })
public class MapAdapter<K, V> extends XmlAdapter<MapAdapter.Adapter<K, V>, Map<K, V>>
{

    @Override
    public Adapter<K, V> marshal(Map<K, V> map) throws Exception {
        if (map == null) {
            return null;
        }
        return new Adapter<K, V>(map);
    }

    public Map<K, V> unmarshal(Adapter<K, V> adapter) throws Exception {
        throw new UnsupportedOperationException("Unmarshalling a list into a map is not supported");
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Adapter", namespace = "MapAdapter")
    public static final class Adapter<K, V>
    {
        List<MapElement<K, V>> item;

        public Adapter() {
        }

        public Adapter(Map<K, V> map) {
            item = new ArrayList<MapElement<K, V>>(map.size());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                item.add(new MapElement<K, V>(entry));
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "MapElement", namespace = "MapAdapter")
    public static final class MapElement<K, V>
    {
        @XmlAnyElement
        private K key;
        @XmlAnyElement
        private V value;

        public MapElement() {
        };

        public MapElement(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public MapElement(Map.Entry<K, V> entry) {
            key = entry.getKey();
            value = entry.getValue();
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }
}
