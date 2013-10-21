/*
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.io;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.ResourceNotAvailableException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class MockDatasource implements IDataSource {

    Map<URI,MockFile> persistentCache = new HashMap<URI,MockFile>();
    
    @Override
    public int canRead(IFileFragment ff) {
        return 1;
    }

    private MockFile getCache(IFileFragment f) {
        if(persistentCache.containsKey(f.getUri())) {
            return persistentCache.get(f.getUri());
        }
		MockFile mf = new MockFile(f.getUri());
        persistentCache.put(f.getUri(), mf);
        return mf;
    }
    
    @Override
    public ArrayList<Array> readAll(IFileFragment f) throws IOException, ResourceNotAvailableException {
        ArrayList<Array> al = new ArrayList<Array>();
        for(IVariableFragment frag:f) {
            al.add(getCache(f).getChild(frag.getName()));
        }
        return al;
    }

    @Override
    public ArrayList<Array> readIndexed(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        return getCache(f.getParent()).getIndexedChild(f.getName()); 
    }

    @Override
    public Array readSingle(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        return getCache(f.getParent()).getChild(f.getName());
    }

    @Override
    public ArrayList<IVariableFragment> readStructure(IFileFragment f) throws IOException {
		if(getCache(f).keys().isEmpty()) {
			return new ArrayList<IVariableFragment>(0);
		}
		ArrayList<IVariableFragment> children = new ArrayList<IVariableFragment>();
		for(String key: getCache(f).keys()) {
			if(!f.hasChild(key)) {
				VariableFragment vf = new VariableFragment(f, key);
				vf.setArray(getCache(f).getChild(key));
			}
		}
		return children;
    }

    @Override
    public IVariableFragment readStructure(IVariableFragment f) throws IOException, ResourceNotAvailableException {
        if(getCache(f.getParent()).keys().contains(f.getName())) {
            return f;
        }
		throw new ResourceNotAvailableException("Variable "+f.getName()+" does not exist on file!");
    }

    @Override
    public List<String> supportedFormats() {
        return Arrays.asList("nc", "nc.gz", "nc.z", "nc.zip", "nc.gzip", "nc.bz2", "cdf", "cdf.gz", "cdf.z", "cdf.zip", "cdf.gzip", "cdf.bz2");
    }

    @Override
    public boolean write(IFileFragment f) {
		log.info("Writing file fragment {}",f.getUri());
		MockFile mf = getCache(f);
        for(IVariableFragment v:f.getImmediateChildren()) {
			log.info("Writing variable fragment {}. Indexed: {}",v.getName(),v.getIndex()!=null);
            if(v.getIndex()!=null) {
				mf.addChild(v.getName(), new ArrayList<Array>(v.getIndexedArray()));
            }else{
                mf.addChild(v.getName(), new ArrayList<Array>(Arrays.asList(v.getArray())));
            }
        }
        return true;
    }

    @Override
    public void configure(Configuration cfg) {
        
    }

    @Override
    public void configurationChanged(ConfigurationEvent ce) {
        
    }
    
}
