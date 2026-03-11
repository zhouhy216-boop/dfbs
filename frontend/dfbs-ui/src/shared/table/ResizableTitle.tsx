/**
 * Phase-1 统一表格：表头列宽拖拽（Excel 式：列边界缝为拖拽热点，cursor col-resize）
 */
import React from 'react';
import { Resizable } from 'react-resizable';
import 'react-resizable/css/styles.css';

export interface ResizableTitleProps {
  onResize?: (e: React.SyntheticEvent, data: { size: { width: number; height: number } }) => void;
  width?: number;
  [key: string]: unknown;
}

export function ResizableTitle(props: ResizableTitleProps) {
  const { width = 0, onResize, ...restProps } = props;
  if (!width || !onResize) {
    return <th {...restProps} />;
  }
  return (
    <Resizable
      width={width}
      height={0}
      axis="x"
      resizeHandles={['e']}
      onResize={onResize}
      draggableOpts={{ enableUserSelectHack: false }}
      minConstraints={[60, 0]}
    >
      <th
        {...restProps}
        style={{
          ...((restProps.style as React.CSSProperties) || {}),
          width,
          minWidth: width,
          maxWidth: width,
          position: 'relative',
        }}
      />
    </Resizable>
  );
}
