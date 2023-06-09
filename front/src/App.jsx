import './App.css'
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  ReactFlowProvider,
  applyEdgeChanges,
  applyNodeChanges, useReactFlow, useStoreApi, useOnSelectionChange
} from 'reactflow'
import 'reactflow/dist/style.css';
import useSWR from "swr";
import React, { useCallback, useState } from 'react'
import {HamburgerIcon} from '@chakra-ui/icons'
import dagre from 'dagre';
import {
  Drawer,
  DrawerBody,
  DrawerContent,
  DrawerHeader,
  DrawerOverlay,
  IconButton,
  List,
  ListIcon,
  ListItem,
  useDisclosure
} from '@chakra-ui/react'
import {VscFileCode, VscPlay} from "react-icons/vsc";
import ToolBar from './ToolBar.jsx'

const fetcher = (url) => fetch(url).then((res) => res.json());

async function postData(url, data) {
  const response = await fetch(url, {
    method: 'POST',
    mode: 'cors',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  })
  return response.json();
}

const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const nodeWidth = 172;
const nodeHeight = 36;
const getLayoutedElements = (nodes, edges, direction = 'TB') => {
  const isHorizontal = direction === 'LR';
  dagreGraph.setGraph({ rankdir: direction });

  nodes.forEach((node) => {
    dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
  });

  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  dagre.layout(dagreGraph);

  nodes.forEach((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    node.targetPosition = isHorizontal ? 'left' : 'top';
    node.sourcePosition = isHorizontal ? 'right' : 'bottom';

    // We are shifting the dagre node position (anchor=center center) to the top left
    // so it matches the React Flow node anchor point (top left).
    node.position = {
      x: nodeWithPosition.x - nodeWidth / 2,
      y: nodeWithPosition.y - nodeHeight / 2,
    };

    return node;
  });

  return { nodes, edges };
};
function App() {
  const { data : classes } = useSWR(
      "http://localhost:8080/api/classes",
      fetcher
  );
  const { zoomIn, zoomOut, setCenter } = useReactFlow();
  const store = useStoreApi();
  const [methods, setMethods] = useState({});
  const [edges, setEdges] = useState([]);
  const [nodes, setNodes] = useState([]);
  const [selectedMethod, setSelectedMethodMethod] = useState(null);
  const { isOpen, onOpen, onClose } = useDisclosure()
  const onMethodSelect = async (signature) => {
    const res = await postData(`http://localhost:8080/api/methodCalls`, signature);
    const newEdges = res.edges.map(it => {
        return {
          id: `${it.from.displayString}-${it.to.displayString}`,
          source: it.from.displayString,
          target: it.to.displayString,
          animated: true,
          type: 'default'
        }
    })
    let stackNodeCount = {}
    const newNodes = res.nodes.map(it => {
        if (stackNodeCount[`${it.depth}`]) {
          stackNodeCount[`${it.depth}`] = stackNodeCount[`${it.depth}`] + 1
        } else {
          stackNodeCount[`${it.depth}`] = 1
        }

      return {
        id: it.method.displayString,
        data: { label: it.method.simpleName },
        draggable: true,
        style: {
          backgroundColor: stackNodeCount[`${it.depth}`] === 1 && it.depth === 0 ? "#ffb19f" : ""
        }
      }
    })

    const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
      newNodes,
      newEdges,
      "LR"
    );

    setNodes(layoutedNodes)
    setEdges(layoutedEdges)
    setSelectedMethodMethod(signature)
  }

  const onClassSelect = async (clazz) => {
    methods[clazz.fqn] = await fetcher(`http://localhost:8080/api/methods?class=${clazz.fqn}`)
    setMethods(JSON.parse(JSON.stringify(methods)))
  }

  const onNodesChange = useCallback(
    (changes) => setNodes((nds) => applyNodeChanges(changes, nds)),
    []
  );
  const onEdgesChange = useCallback(
    (changes) => setEdges((eds) => applyEdgeChanges(changes, eds)),
    []
  );

  const onMoveToSelectMethod = e => {
    const { nodeInternals } = store.getState();
    const target = Array.from(nodeInternals)
      .map(([, node]) => node)
      .find(node => node.id === selectedMethod.displayString)
    if (target) {
      const x = target.position.x + target.width / 2;
      const y = target.position.y + target.height / 2;
      const zoom = 1.85;
      setCenter(x, y, { zoom, duration: 1000 });
    }
  };

  useOnSelectionChange({
    onChange: ({ nodes, edges }) => {

    },
  })
  return (
    <div className="app">
      <div className="header">
        <IconButton icon={<HamburgerIcon/>} onClick={onOpen}/>
        <p className="selected-method-name">{selectedMethod?.displayString || "None"}</p>
      </div>
      <Drawer placement='left' onClose={onClose} isOpen={isOpen}>
        <DrawerOverlay />
        <DrawerContent>
          <DrawerHeader borderBottomWidth='1px'>Method Chooser</DrawerHeader>
          <DrawerBody style={{overflowX: "scroll"}}>
            <List spacing={1}>
              {classes?.map(it => (
                <ListItem key={it.fqn} onClick={e => onClassSelect(it)}>
                  <div style={{display: "flex", alignItems: "center"}}>
                    <ListIcon as={VscFileCode} color='green.500' />
                    <p>{it.simple}</p>
                  </div>
                  <List spacing={1} style={{marginLeft: "16px"}}>
                    {methods[it.fqn]?.map(m => (
                      <ListItem key={m.signature.displayString} onClick={e => onMethodSelect(m.signature)} style={{display: "flex", alignItems: "center"}}>
                        <ListIcon as={VscPlay} color='green.500' />
                        <p>{m.signature.simpleName}</p>
                      </ListItem>
                    ))}
                  </List>
                </ListItem>))}
            </List>
          </DrawerBody>
        </DrawerContent>
      </Drawer>
      <div className="flow-wrapper">
        <ReactFlow
          nodes={nodes}
          onNodesChange={onNodesChange}
          edges={edges}
          onEdgesChange={onEdgesChange}
          fitView>
          <Background />
          <Controls/>
          <MiniMap/>
          <ToolBar onMoveToSelectMethod={onMoveToSelectMethod}/>
        </ReactFlow>
      </div>
    </div>
  )
}

export default App
