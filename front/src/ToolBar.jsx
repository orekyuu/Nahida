import {Panel} from "reactflow";
import React from "react";
import "./ToolBar.css"
import { IconButton } from '@chakra-ui/react'
import { VscEye } from 'react-icons/vsc'

export default function ToolBar({ onMoveToSelectMethod }) {
    return (<Panel position="bottom-center">
        <div className="panel-wrapper">
            <IconButton icon={<VscEye/>} onClick={onMoveToSelectMethod}/>
        </div>
    </Panel>)
}