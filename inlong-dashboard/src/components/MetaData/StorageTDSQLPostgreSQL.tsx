/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import {
  getColsFromFields,
  GetStorageColumnsType,
  GetStorageFormFieldsType,
} from '@/utils/metaData';
import { ColumnsType } from 'antd/es/table';
import EditableTable, { ColumnsItemProps } from '@/components/EditableTable';
import i18n from '@/i18n';
import { excludeObject } from '@/utils';
import { sourceDataFields } from './SourceDataFields';

// tdsqlpostgreSQLFieldTypes
const tdsqlpostgreSQLFieldTypes = [
  'SMALLINT',
  'SMALLSERIAL',
  'INT2',
  'SERIAL2',
  'INTEGER',
  'SERIAL',
  'BIGINT',
  'BIGSERIAL',
  'REAL',
  'FLOAT4',
  'FLOAT8',
  'DOUBLE',
  'NUMERIC',
  'DECIMAL',
  'BOOLEAN',
  'DATE',
  'TIME',
  'TIMESTAMP',
  'CHAR',
  'CHARACTER',
  'VARCHAR',
  'TEXT',
  'BYTEA',
].map(item => ({
  label: item,
  value: item,
}));

const getForm: GetStorageFormFieldsType = (
  type,
  { currentValues, inlongGroupId, isEdit, dataType, form } = {} as any,
) => {
  const fileds = [
    {
      type: 'input',
      label: 'JDBC URL',
      name: 'jdbcUrl',
      rules: [{ required: true }],
      props: {
        placeholder: 'jdbc:postgresql://127.0.0.1:5432/db_name',
        disabled: isEdit && [110, 130].includes(currentValues?.status),
        style: { width: 500 },
      },
    },
    {
      type: 'input',
      label: i18n.t('components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.SchemaName'),
      name: 'schemaName',
      rules: [{ required: true }],
      props: {
        disabled: isEdit && [110, 130].includes(currentValues?.status),
      },
      _inTable: true,
    },
    {
      type: 'input',
      label: i18n.t('components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.TableName'),
      name: 'tableName',
      rules: [{ required: true }],
      props: {
        disabled: isEdit && [110, 130].includes(currentValues?.status),
      },
      _inTable: true,
    },
    {
      type: 'input',
      label: i18n.t('components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.PrimaryKey'),
      name: 'primaryKey',
      rules: [{ required: true }],
      props: {
        disabled: isEdit && [110, 130].includes(currentValues?.status),
      },
      _inTable: true,
    },
    {
      type: 'radio',
      label: i18n.t('components.AccessHelper.StorageMetaData.EnableCreateResource'),
      name: 'enableCreateResource',
      rules: [{ required: true }],
      initialValue: 1,
      tooltip: i18n.t('components.AccessHelper.StorageMetaData.EnableCreateResourceHelp'),
      props: {
        disabled: isEdit && [110, 130].includes(currentValues?.status),
        options: [
          {
            label: i18n.t('basic.Yes'),
            value: 1,
          },
          {
            label: i18n.t('basic.No'),
            value: 0,
          },
        ],
      },
    },
    {
      type: 'input',
      label: i18n.t('components.AccessHelper.StorageMetaData.Username'),
      name: 'username',
      rules: [{ required: true }],
      props: {
        disabled: isEdit && [110, 130].includes(currentValues?.status),
      },
      _inTable: true,
    },
    {
      type: 'password',
      label: i18n.t('components.AccessHelper.StorageMetaData.Password'),
      name: 'password',
      rules: [{ required: true }],
      props: {
        disabled: isEdit && [110, 130].includes(currentValues?.status),
        style: {
          maxWidth: 500,
        },
      },
    },
    {
      type: (
        <EditableTable
          size="small"
          columns={getFieldListColumns(dataType, currentValues)}
          canDelete={(record, idx, isNew) => !isEdit || isNew}
        />
      ),
      name: 'sinkFieldList',
    },
  ];

  return type === 'col'
    ? getColsFromFields(fileds)
    : fileds.map(item => excludeObject(['_inTable'], item));
};

const getFieldListColumns: GetStorageColumnsType = (dataType, currentValues) => {
  return [
    ...sourceDataFields,
    {
      title: `TDSQLPOSTGRESQL${i18n.t(
        'components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.FieldName',
      )}`,
      dataIndex: 'fieldName',
      initialValue: '',
      rules: [
        { required: true },
        {
          pattern: /^[a-z][0-9a-z_]*$/,
          message: i18n.t('components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.FieldNameRule'),
        },
      ],
      props: (text, record, idx, isNew) => ({
        disabled: [110, 130].includes(currentValues?.status as number) && !isNew,
      }),
    },
    {
      title: `TDSQLPOSTGRESQL${i18n.t(
        'components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.FieldType',
      )}`,
      dataIndex: 'fieldType',
      initialValue: tdsqlpostgreSQLFieldTypes[0].value,
      type: 'select',
      props: (text, record, idx, isNew) => ({
        options: tdsqlpostgreSQLFieldTypes,
        disabled: [110, 130].includes(currentValues?.status as number) && !isNew,
      }),
      rules: [{ required: true }],
    },
    {
      title: i18n.t('components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.IsMetaField'),
      initialValue: 0,
      dataIndex: 'isMetaField',
      type: 'select',
      props: (text, record, idx, isNew) => ({
        options: [
          {
            label: i18n.t('basic.Yes'),
            value: 1,
          },
          {
            label: i18n.t('basic.No'),
            value: 0,
          },
        ],
      }),
    },
    {
      title: i18n.t('components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.FieldFormat'),
      dataIndex: 'fieldFormat',
      initialValue: '',
      type: 'autocomplete',
      props: (text, record, idx, isNew) => ({
        options: ['MICROSECONDS', 'MILLISECONDS', 'SECONDS', 'SQL', 'ISO_8601'].map(item => ({
          label: item,
          value: item,
        })),
      }),
      visible: (text, record) =>
        ['BIGINT', 'DATE', 'TIMESTAMP'].includes(record.fieldType as string),
    },
    {
      title: i18n.t('components.AccessHelper.StorageMetaData.TDSQLPostgreSQL.FieldDescription'),
      dataIndex: 'fieldComment',
      initialValue: '',
    },
  ] as ColumnsItemProps[];
};

const tableColumns = getForm('col') as ColumnsType;

export const StorageTDSQLPostgreSQL = {
  getForm,
  getFieldListColumns,
  tableColumns,
};
