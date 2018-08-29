/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */

package org.acumos.bporchestrator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.acumos.bporchestrator.model.*;

import org.acumos.bporchestrator.splittercollator.vo.*;

import org.junit.Test;

public class PojoTest {

	@Test
	public void testPojos() {

		Blueprint bp = new Blueprint();
		bp.toString();

		org.acumos.bporchestrator.model.CollatorMap cm = new org.acumos.bporchestrator.model.CollatorMap();
		cm.setCollator_type(null);
		cm.setMap_inputs(null);
		cm.setMap_outputs(null);
		cm.setOutput_message_signature(null);
		String ctype = cm.getCollator_type();
		String opms = cm.getOutput_message_signature();
		CollatorMapInput[] cmip = cm.getMap_inputs();
		CollatorMapOutput[] cmop = cm.getMap_outputs();
		cm.toString();

		DataBroker db = new DataBroker();
		db.setName(null);
		db.setOperationSignature(null);
		String dbname = db.getName();
		OperationSignature dbos = db.getOperationSignature();
		db.toString();

		DataBrokerMap dbm = new DataBrokerMap();
		dbm.setCsvFileFieldSeparator(null);
		dbm.setDataBrokerType(null);
		dbm.setFirstRow(null);
		dbm.setLocalSystemDataFilePath(null);
		dbm.setMapInputs(null);
		dbm.setMapOutputs(null);
		dbm.setScript(null);
		dbm.setTargetSystemUrl(null);
		String csv = dbm.getCsvFileFieldSeparator();
		String dbtype = dbm.getDataBrokerType();
		String firstrow = dbm.getFirstRow();
		String filepath = dbm.getLocalSystemDataFilePath();
		MapInputs[] dbmi = dbm.getMapInputs();
		MapOutputs[] dbmo = dbm.getMapOutputs();
		String scriptstring = dbm.getScript();
		String url = dbm.getTargetSystemUrl();
		dbm.toString();

		DataSource ds = new DataSource();
		ds.setName(null);
		ds.setOperationSignature(null);
		String dsname = ds.getName();
		OperationSignature dsosname = ds.getOperationSignature();
		ds.toString();

		DockerInfo di = new DockerInfo();
		di.setContainer(null);
		di.setIpAddress(null);
		di.setPort(null);
		String contname = di.getContainer();
		String ipadd = di.getIpAddress();
		String port = di.getPort();
		di.toString();

		DockerInfoList dil = new DockerInfoList();
		dil.setDockerList(null);
		ArrayList<DockerInfo> dklist = dil.getDockerList();
		dil.toString();

		InputField inpf = new InputField();
		inpf.setChecked(null);
		inpf.setErrorIndicator(null);
		inpf.setMappedToField(null);
		inpf.setMessageSignature(null);
		inpf.setName(null);
		inpf.setOtherAttributes(null);
		inpf.setParameterName(null);
		inpf.setParameterRole(null);
		inpf.setParameterTag(null);
		inpf.setParameterType(null);
		inpf.setSourceName(null);
		inpf.setType(null);

		String errorInd = inpf.getErrorIndicator();
		String getchecked = inpf.getChecked();
		String mappedfield = inpf.getMappedToField();
		String msgsigna = inpf.getMessageSignature();
		String inpfname = inpf.getName();
		String inpfotherattr = inpf.getOtherAttributes();
		String inpfparamtername = inpf.getParameterName();
		String inpfparameterrole = inpf.getParameterRole();
		String inpfparametertag = inpf.getParameterTag();
		String inpfparametertype = inpf.getParameterType();
		String inpfsourcename = inpf.getSourceName();
		String inpftype = inpf.getType();
		inpf.toString();

		InputPort ip = new InputPort();
		ip.setContainerName(null);
		ip.setOperationSignature(null);
		String ipcontainername = ip.getContainerName();
		OperationSignature ipops = ip.getOperationSignature();
		ip.toString();

		MapInputs mi = new MapInputs();
		mi.setInputField(null);
		InputField miifs = mi.getInputField();
		mi.toString();

		MapOutputs mo = new MapOutputs();
		mo.setOutputField(null);
		OutputField opf = mo.getOutputField();
		mo.toString();

		MappingTable mt = new MappingTable();
		mt.toString();

		MlModel mlm = new MlModel();
		mlm.setName(null);
		mlm.setOperationSignature(null);
		String mlname = mlm.getName();
		OperationSignature mlops = mlm.getOperationSignature();
		mlm.toString();

		Node n = new Node();
		n.setBeingProcessedByAThread(true);
		n.setCollatorMap(null);
		n.setContainerName(null);
		n.setDataBrokerMap(null);
		n.setDataSources(null);
		n.setImage(null);
		n.setImmediateAncestors(null);
		n.setNodeHeaders(null);
		n.setNodeOutput(null);
		n.setNodeType(null);
		n.setOperationSignatureList(null);
		n.setOutputAvailable(true);
		n.setProtoUri(null);
		n.setSplitterMap(null);
		n.toString();

		boolean processing = n.beingProcessedByAThread;
		org.acumos.bporchestrator.model.CollatorMap ncmp = n.getCollatorMap();
		String ncontainername = n.getContainerName();
		DataBrokerMap ndbmap = n.getDataBrokerMap();
		List<DataSource> ndsources = n.getDataSources();
		String nimage = n.getImage();
		List<Node> nancestors = n.getImmediateAncestors();
		Map<String, List<String>> nheaders = n.getNodeHeaders();
		byte[] noutout = n.getNodeOutput();
		String ntype = n.getNodeType();
		ArrayList<OperationSignatureList> nosl = n.getOperationSignatureList();
		String nuri = n.getProtoUri();
		org.acumos.bporchestrator.model.SplitterMap nsmap = n.getSplitterMap();

		OperationSignature os = new OperationSignature();
		os.setInputMessageName(null);
		os.setOperationName(null);
		os.setOutputMessageName(null);

		os.getInputMessageName();
		os.getOperationName();
		os.getOutputMessageName();
		os.toString();

		OperationSignatureList osl = new OperationSignatureList();
		osl.setConnectedTo(null);
		osl.setOperationSignature(null);
		ArrayList<ConnectedTo> oslconnectedto = osl.getConnectedTo();
		OperationSignature oslops = osl.getOperationSignature();
		osl.toString();

		Orchestrator o = new Orchestrator();
		o.setImage(null);
		o.setName(null);
		o.setVersion(null);
		String oimage = o.getImage();
		String oname = o.getName();
		String oversion = o.getVersion();
		o.toString();

		OutputField of = new OutputField();
		of.setErrorIndicator(null);
		of.setMappedToField(null);
		of.setMessageSignature(null);
		of.setName(null);
		of.setOtherAttributes(null);
		of.setParameterName(null);
		of.setParameterRole(null);
		of.setParameterTag(null);
		of.setParameterType(null);
		of.setTag(null);
		of.setTargetName(null);
		of.setTypeAndRoleHierarchyList(null);

		of.toString();

		ProbeIndicator pi = new ProbeIndicator();
		pi.setValue(null);
		String pivalue = pi.getValue();
		pi.toString();

		org.acumos.bporchestrator.model.SplitterMap sm = new org.acumos.bporchestrator.model.SplitterMap();
		sm.setInput_message_signature(null);
		sm.setMap_inputs(null);
		sm.setMap_outputs(null);
		sm.setSplitter_type(null);

		String smims = sm.getInput_message_signature();
		SplitterMapInput[] smimp = sm.getMap_inputs();
		SplitterMapOutput[] smop = sm.getMap_outputs();
		String smap = sm.getSplitter_type();
		sm.toString();

		TrainingClient tc = new TrainingClient();
		tc.setContainerName(null);
		tc.setDataBrokers(null);
		tc.setImage(null);
		tc.setMlModels(null);
		String tccontainername = tc.getContainerName();
		List<DataBroker> tcdb = tc.getDataBrokers();
		String tcimage = tc.getImage();
		List<MlModel> tcmodelslist = tc.getMlModels();
		tc.toString();

		TypeAndRoleHierarchyList trhl = new TypeAndRoleHierarchyList();
		trhl.setName(null);
		trhl.setRole(null);
		String trhlname = trhl.getName();
		String trhlrole = trhl.getRole();
		trhl.toString();

		Argument a = new Argument();
		a.setComplexType(null);
		a.setName(null);
		a.setRole(null);
		a.setTag(null);
		a.setType(null);
		ComplexType complextype = a.getComplexType();
		String aname = a.getName();
		String arole = a.getRole();
		String atage = a.getTag();
		String atype = a.getType();
		a.toString();

		CollatorInputField cif = new CollatorInputField();
		cif.setError_indicator(null);
		cif.setMapped_to_field(null);
		cif.setMessage_signature(null);
		cif.setParameter_name(null);
		cif.setParameter_tag(null);
		cif.setParameter_type(null);
		cif.setSource_name(null);
		String cifei = cif.getError_indicator();
		String cifgm = cif.getMapped_to_field();
		String cifms = cif.getMessage_signature();
		String cifpn = cif.getParameter_name();
		String cifpt = cif.getParameter_tag();
		String cifptype = cif.getParameter_type();
		String cifsname = cif.getSource_name();
		cif.toString();

		org.acumos.bporchestrator.splittercollator.vo.CollatorMap cmsc = new org.acumos.bporchestrator.splittercollator.vo.CollatorMap();
		cmsc.setCollator_type(null);
		cmsc.setMap_inputs(null);
		cmsc.setMap_outputs(null);
		cmsc.setOutput_message_signature(null);
		String cmscctype = cmsc.getCollator_type();
		CollatorMapInput[] cmscmi = cmsc.getMap_inputs();
		org.acumos.bporchestrator.splittercollator.vo.CollatorMapOutput[] csmscmo = cmsc.getMap_outputs();
		String cmscgos = cmsc.getOutput_message_signature();
		cmsc.toString();

		CollatorMapInput cmi = new CollatorMapInput();
		cmi.setInput_field(null);
		CollatorInputField cmiif = cmi.getInput_field();
		cmi.toString();

		CollatorMapOutput cmo = new CollatorMapOutput();
		cmo.setOutput_field(null);
		CollatorOutputField cmoof = cmo.getOutput_field();
		cmo.toString();

		CollatorOutputField cof = new CollatorOutputField();
		cof.setParameter_name(null);
		cof.setParameter_tag(null);
		cof.setParameter_type(null);

		String cofpn = cof.getParameter_name();
		String coftage = cof.getParameter_tag();
		String cofptype = cof.getParameter_type();
		cof.toString();

		ComplexType ct = new ComplexType();
		ct.setMessageargumentList(null);
		ct.setMessageName(null);
		List<MessageargumentList> msal = ct.getMessageargumentList();
		String ctmsgname = ct.getMessageName();
		ct.toString();

		Configuration c = new Configuration();
		c.setMap_inputs(null);
		c.setMap_outputs(null);
		c.setProtobufFileStr(null);
		CollatorMapInput[] cgmap = c.getMap_inputs();
		CollatorMapOutput[] cgmapoutputs = c.getMap_outputs();
		String gprobstr = c.getProtobufFileStr();
		c.toString();

		Message m = new Message();
		m.setMessageargumentList(null);
		m.setMessageName(null);
		Argument[] mgemsig = m.getMessageargumentList();
		String mgetmsgname = m.getMessageName();
		m.toString();

		MessageargumentList mal = new MessageargumentList();
		mal.setComplexType(null);
		mal.setName(null);
		mal.setRole(null);
		mal.setTag(null);
		mal.setType(null);
		ComplexType malgetct = mal.getComplexType();
		String malgetname = mal.getName();
		String malgetrole = mal.getRole();
		String malgettage = mal.getTag();
		String malgettype = mal.getType();
		mal.toString();

		Protobuf pf = new Protobuf();
		pf.setMessages(null);
		pf.setOptions(null);
		pf.setService(null);
		pf.setSyntax(null);
		List<ProtobufMessage> lopbms = pf.getMessages();
		List<ProtobufOption> lopbops = pf.getOptions();
		ProtobufService pbuffgetservice = pf.getService();
		String pbugggetsyntax = pf.getSyntax();
		// pf.toString();

		ProtobufMessage pm = new ProtobufMessage();
		pm.setFields(null);
		pm.setName(null);
		List<ProtobufMessageField> pmgetfields = pm.getFields();
		String pmgetname = pm.getName();
		// pm.toString();

		ProtobufMessageField pmf = new ProtobufMessageField();
		pmf.setName(null);
		pmf.setRole(null);
		pmf.setTag(0);
		pmf.setType(null);
		String pmfname = pmf.getName();
		String pmfrole = pmf.getRole();
		int pmftag = pmf.getTag();
		String pmftype = pmf.getType();
		pmf.toString();

		ProtobufOption po = new ProtobufOption();
		po.setName(null);
		po.setValue(null);
		String poname = po.getName();
		String povalue = po.getValue();
		po.toString();

		ProtobufService ps = new ProtobufService();
		ps.setName(null);
		ps.setOperations(null);
		String psname = ps.getName();
		List<ProtobufServiceOperation> psgetops = ps.getOperations();
		// ps.toString();

		ProtobufServiceOperation pso = new ProtobufServiceOperation();
		pso.setInputMessageNames(null);
		pso.setName(null);
		pso.setOutputMessageNames(null);
		pso.setType(null);
		List<String> psoimn = pso.getInputMessageNames();
		String psogn = pso.getName();
		List<String> psopmn = pso.getOutputMessageNames();
		String psogt = pso.getType();
		// pso.toString();

		Result r = new Result();
		r.setMessage(null);
		r.setStatus(0);
		Object resmesg = r.getMessage();
		int res = r.getStatus();
		r.toString();

		SplitterInputField sif = new SplitterInputField();
		sif.setParameter_name(null);
		sif.setParameter_role(null);
		sif.setParameter_tag(null);
		sif.setParameter_type(null);
		String sifpn = sif.getParameter_name();
		String sifpr = sif.getParameter_role();
		String sifptag = sif.getParameter_tag();
		String sifptype = sif.getParameter_type();
		sif.toString();

		org.acumos.bporchestrator.splittercollator.vo.SplitterMap smsc = new org.acumos.bporchestrator.splittercollator.vo.SplitterMap();
		smsc.setInput_message_signature(null);
		smsc.setMap_inputs(null);
		smsc.setMap_outputs(null);
		smsc.setSplitter_type(null);
		String smscims = smsc.getInput_message_signature();
		SplitterMapInput[] smscsmip = smsc.getMap_inputs();
		SplitterMapOutput[] smscsmop = smsc.getMap_outputs();
		String smsctype = smsc.getSplitter_type();
		smsc.toString();

		SplitterMapInput smi = new SplitterMapInput();
		smi.setInput_field(null);
		SplitterInputField smiif = smi.getInput_field();
		smi.toString();

		SplitterMapOutput smo = new SplitterMapOutput();
		smo.setOutput_field(null);
		SplitterOutputField smoof = smo.getOutput_field();
		smo.toString();

		SplitterOutputField sof = new SplitterOutputField();
		sof.setError_indicator(null);
		sof.setMapped_to_field(null);
		sof.setMessage_signature(null);
		sof.setParameter_name(null);
		sof.setParameter_role(null);
		sof.setParameter_tag(null);
		sof.setParameter_type(null);
		sof.setTarget_name(null);
		String soferror = sof.getError_indicator();
		String sofmaptofield = sof.getMapped_to_field();
		sof.getMessage_signature();
		String sofparametername = sof.getParameter_name();
		String sofparameterrole = sof.getParameter_role();
		String sofparamtertag = sof.getParameter_tag();
		String sofparametertype = sof.getParameter_type();
		String softargetname = sof.getTarget_name();
		sof.toString();

	}

}